package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchonOfValorsReach.class, GrizzlyBears.class, Ornithopter.class, Shock.class, Zombify.class})
class ArchonOfValorsReachTest extends BaseCardTest {

    @Test
    void choosesOnlyPrintedCardTypes() {
        harness.castFromHand(player1, new ArchonOfValorsReach(), "{4}{G}{W}");
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly(
                CardType.ENCHANTMENT.name(),
                CardType.SORCERY.name(),
                CardType.INSTANT.name(),
                CardType.ARTIFACT.name(),
                CardType.PLANESWALKER.name());
    }

    @Test
    void playersCannotCastSpellsOfTheChosenType() {
        addReadyArchon(player1, CardType.INSTANT);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void spellsOfOtherTypesRemainCastable() {
        addReadyArchon(player1, CardType.ARTIFACT);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void chosenArtifactSpellsAreAlsoRestricted() {
        addReadyArchon(player1, CardType.ARTIFACT);
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @ParameterizedTest
    @EnumSource(value = CardType.class, names = {"ARTIFACT", "ENCHANTMENT", "INSTANT", "SORCERY", "PLANESWALKER"})
    void storesEachChoiceAsItEnters(CardType chosenType) {
        harness.castFromHand(player1, new ArchonOfValorsReach(), "{4}{G}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Archon of Valor's Reach");
        harness.handleListChoice(player1, chosenType.name());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(archon -> assertThat(archon.getChosenCardType()).isEqualTo(chosenType));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reanimatedArchonChoosesATypeAndRestrictsCasting() {
        ArchonOfValorsReach archon = new ArchonOfValorsReach();
        harness.setGraveyard(player1, List.of(archon));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, archon.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, CardType.ARTIFACT.name());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void restrictionEndsWhenArchonLeavesTheBattlefield() {
        Permanent archon = addReadyArchon(player1, CardType.ARTIFACT);
        gd.playerBattlefields.get(player1.getId()).remove(archon);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new Ornithopter(), "{0}");

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addReadyArchon(Player controller, CardType chosenType) {
        Permanent archon = harness.addToBattlefieldAndReturn(controller, new ArchonOfValorsReach());
        archon.setSummoningSick(false);
        archon.setChosenCardType(chosenType);
        return archon;
    }
}

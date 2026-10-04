package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.NyxbornWolf;
import com.github.laxika.magicalvibes.cards.t.TempleOfPlenty;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gild.class, NyxbornWolf.class, TempleOfPlenty.class})
class GildTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target creature and creates a Gold token")
    void exilesCreatureAndCreatesGoldToken() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new NyxbornWolf()).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Gild()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Nyxborn Wolf");
        harness.assertNotInGraveyard(player2, "Nyxborn Wolf");

        List<Permanent> goldTokens = findPermanents(player1, "Gold");
        assertThat(goldTokens).hasSize(1);
        assertThat(goldTokens.getFirst().getCard().isToken()).isTrue();
        assertThat(goldTokens.getFirst().getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(goldTokens.getFirst().getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Gold token can be sacrificed for mana of any color")
    void goldTokenProducesMana() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new NyxbornWolf()).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Gild()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        assertThat(findPermanents(player1, "Gold")).isEmpty();

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new TempleOfPlenty()).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Gild()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Created Gold token has the Gold artifact subtype")
    void goldTokenHasGoldSubtype() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new NyxbornWolf()).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Gild()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Gold").getCard().getSubtypes())
                .extracting(Enum::name)
                .contains("GOLD");
    }

    @Test
    @DisplayName("No Gold is created when the target leaves before resolution")
    void noGoldWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornWolf());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Gild()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gold");
        harness.assertNotOnBattlefield(player2, "Gold");
        harness.assertInGraveyard(player1, "Gild");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can exile your own creature and creates Gold for the caster")
    void canExileOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NyxbornWolf());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Gild()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nyxborn Wolf");
        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card()).isSameAs(target.getCard());
            assertThat(entry.ownerId()).isEqualTo(player1.getId());
        });
        assertThat(findPermanents(player1, "Gold")).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Gold");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("A tapped Gold token can immediately produce any color without using the stack")
    void tappedGoldProducesAnyColor(ManaColor color) {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new NyxbornWolf()).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Gild()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        findPermanent(player1, "Gold").tap();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        harness.assertNotOnBattlefield(player1, "Gold");
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}

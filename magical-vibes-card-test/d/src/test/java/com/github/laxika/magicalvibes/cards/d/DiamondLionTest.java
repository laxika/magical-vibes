package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LoseFocus;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiamondLion.class, OrnithopterOfParadise.class, LoseFocus.class})
class DiamondLionTest extends BaseCardTest {

    @Test
    @DisplayName("Activating discards the hand, sacrifices itself, and adds three mana of the chosen color")
    void activateDiscardsHandSacrificesAndAddsThreeMana() {
        addCreatureReady(player1, new DiamondLion());
        harness.setHand(player1, List.of(new OrnithopterOfParadise(), new OrnithopterOfParadise()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Activating with an empty hand still produces three mana")
    void activateWithEmptyHandStillProducesMana() {
        addCreatureReady(player1, new DiamondLion());
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new DiamondLion());
        harness.setHand(player1, List.of(new OrnithopterOfParadise()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("summoning sickness");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Diamond Lion");
    }

    @Test
    void cannotActivateWhenTapped() {
        addCreatureReady(player1, new DiamondLion()).tap();
        harness.setHand(player1, List.of(new OrnithopterOfParadise()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("already tapped");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Diamond Lion");
    }

    @Test
    void cannotActivateDuringCounterspellManaPayment() {
        addCreatureReady(player1, new DiamondLion());
        OrnithopterOfParadise spell = new OrnithopterOfParadise();
        harness.castFromHand(player1, spell, "{2}");
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player1, List.of(new OrnithopterOfParadise()));
        harness.setHand(player2, List.of(new LoseFocus()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstantWithRepeatedCosts(player2, 0, spell.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThatThrownBy(() -> gs.activateAbility(gd, player1, 0, 0, null, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Diamond Lion");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInGraveyard(player1, "Ornithopter of Paradise");
    }

    @Test
    void ordinaryManaAbilityCanActivateDuringCounterspellManaPayment() {
        addCreatureReady(player1, new OrnithopterOfParadise());
        OrnithopterOfParadise spell = new OrnithopterOfParadise();
        harness.castFromHand(player1, spell, "{2}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new LoseFocus()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstantWithRepeatedCosts(player2, 0, spell.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        gs.activateAbility(gd, player1, 0, 0, null, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInGraveyard(player1, "Ornithopter of Paradise");
    }
}

package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VraskaTheSilencer.class, LlanowarElves.class, Shock.class})
class VraskaTheSilencerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying returns the opponent's nontoken creature as a tapped Treasure")
    void payingReturnsCreatureAsTappedTreasure() {
        harness.addToBattlefield(player1, new VraskaTheSilencer());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID dyingId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, dyingId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Llanowar Elves");
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(returned.getCard().getSubtypes()).containsExactly(CardSubtype.TREASURE);
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(gs.getEffectiveActivatedAbilities(gd, returned)).hasSize(1);
    }

    @Test
    @DisplayName("The returned Treasure has only its sacrifice-for-mana ability")
    void returnedTreasureProducesManaAndSacrifices() {
        harness.addToBattlefield(player1, new VraskaTheSilencer());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Llanowar Elves"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Llanowar Elves");
        returned.untap();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(returned), 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(returned);
    }
}

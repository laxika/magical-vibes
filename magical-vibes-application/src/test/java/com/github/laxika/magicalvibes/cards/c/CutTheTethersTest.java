package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BlindWithAnger;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CutTheTethers.class, BlindWithAnger.class, LanternKami.class, DevotedRetainer.class})
class CutTheTethersTest extends BaseCardTest {

    @Test
    @DisplayName("Declining the payment returns the Spirit to its owner's hand")
    void decliningBouncesTheSpirit() {
        harness.addToBattlefield(player2, new LanternKami());
        castCutTheTethers();

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInHand(player2, "Lantern Kami");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Paying {3} keeps the Spirit on the battlefield")
    void payingKeepsTheSpirit() {
        harness.addToBattlefield(player2, new LanternKami());
        castCutTheTethers();

        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Lantern Kami");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Accepting the payment without enough mana still returns the Spirit")
    void acceptingWithoutEnoughManaBouncesTheSpirit() {
        harness.addToBattlefield(player2, new LanternKami());
        castCutTheTethers();

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInHand(player2, "Lantern Kami");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Spirit is a separate payment — paying for one still bounces the other")
    void eachSpiritIsAnIndependentPayment() {
        harness.addToBattlefield(player2, new LanternKami());
        harness.addToBattlefield(player2, new LanternKami());
        castCutTheTethers();

        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(countPermanents(player2, "Lantern Kami")).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-Spirit permanents are untouched and never prompted for")
    void nonSpiritsAreUntouched() {
        harness.addToBattlefield(player2, new DevotedRetainer());
        castCutTheTethers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Devoted Retainer");
    }

    @Test
    @DisplayName("The caster's own Spirits are put to the same choice")
    void casterIsNotSpared() {
        harness.addToBattlefield(player1, new LanternKami());
        harness.addToBattlefield(player2, new LanternKami());
        castCutTheTethers();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInHand(player1, "Lantern Kami");
        harness.assertInHand(player2, "Lantern Kami");
    }

    @Test
    @DisplayName("A stolen Spirit is decided by its owner, not by the player controlling it")
    void stolenSpiritIsDecidedByItsOwner() {
        Permanent spirit = stealPlayer2Spirit();

        castCutTheTethers();

        // The prompt goes to the owner, not to player1 who controls the Spirit
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInHand(player2, "Lantern Kami");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("The owner of a stolen Spirit pays to keep it, and it stays under the thief's control")
    void owningPlayerPaysForAStolenSpirit() {
        Permanent spirit = stealPlayer2Spirit();

        castCutTheTethers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(spirit.getId()));
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    /** Player 2 owns the Lantern Kami; player 1 takes control of it with Blind with Anger. */
    private Permanent stealPlayer2Spirit() {
        Permanent spirit = addCreatureReady(player2, new LanternKami());

        harness.setHand(player1, List.of(new BlindWithAnger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, spirit.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(spirit.getId()));
        assertThat(gd.stolenCreatures).containsEntry(spirit.getId(), player2.getId());
        return spirit;
    }

    private void castCutTheTethers() {
        harness.castFromHand(player1, new CutTheTethers(), "{2}{U}{U}");
        harness.passBothPriorities();
    }
}

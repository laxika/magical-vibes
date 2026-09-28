package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({K9MarkI.class, GiantGrowth.class, GrizzlyBears.class, IsamaruHoundOfKonda.class})
class K9MarkITest extends BaseCardTest {

    @Test
    @DisplayName("Untapped K-9 grants ward to other legendary creatures you control")
    void grantsWardToOtherLegendaryCreatures() {
        addReadyPermanent(player1, new K9MarkI());
        Permanent legendary = addReadyPermanent(player1, new IsamaruHoundOfKonda());

        castGrowthAt(player2, legendary, 2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        assertThat(legendary.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("K-9 does not give ward to itself")
    void doesNotGrantWardToItself() {
        Permanent k9 = addReadyPermanent(player1, new K9MarkI());

        castGrowthAt(player2, k9, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(k9.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Tapping K-9 turns off its ward grant")
    void tappingK9TurnsOffWardGrant() {
        Permanent k9 = addReadyPermanent(player1, new K9MarkI());
        Permanent legendary = addReadyPermanent(player1, new IsamaruHoundOfKonda());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, legendary.getId());
        harness.passBothPriorities();

        assertThat(k9.isTapped()).isTrue();
        castGrowthAt(player2, legendary, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(legendary.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("The activation makes a legendary creature unblockable")
    void makesLegendaryCreatureUnblockable() {
        addReadyPermanent(player1, new K9MarkI());
        Permanent attacker = addReadyPermanent(player1, new IsamaruHoundOfKonda());
        addReadyPermanent(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("The activation cannot target a nonlegendary creature")
    void cannotTargetNonlegendaryCreature() {
        addReadyPermanent(player1, new K9MarkI());
        Permanent nonlegendary = addReadyPermanent(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, nonlegendary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    private void castGrowthAt(Player caster, Permanent target, int manaAmount) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new GiantGrowth()));
        harness.addMana(caster, ManaColor.GREEN, manaAmount);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}

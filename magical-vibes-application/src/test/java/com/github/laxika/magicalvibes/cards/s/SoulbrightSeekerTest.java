package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulbrightSeekerTest extends BaseCardTest {

    @Test
    @DisplayName("Without an Elemental, casting requires the additional {2}")
    void requiresAdditionalManaWithoutElemental() {
        harness.setHand(player1, List.of(new SoulbrightSeeker()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An Elemental permanent lets it be cast without the additional mana")
    void beholdElementalPermanentAvoidsAdditionalMana() {
        harness.addToBattlefield(player1, new AirElemental());
        SoulbrightSeeker seeker = new SoulbrightSeeker();
        harness.setHand(player1, List.of(seeker));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(seeker.getId()));
    }

    @Test
    @DisplayName("An Elemental card in hand lets it be cast without the additional mana")
    void beholdElementalCardAvoidsAdditionalMana() {
        SoulbrightSeeker seeker = new SoulbrightSeeker();
        AirElemental elemental = new AirElemental();
        harness.setHand(player1, List.of(seeker, elemental));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(elemental.getId()));
    }

    @Test
    @DisplayName("The targeted ability grants trample until end of turn")
    void grantsTrampleUntilEndOfTurn() {
        Permanent seeker = addSeekerReady(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        activateAndResolve(seeker, target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The bonus mana happens only on the third resolution")
    void addsManaOnlyOnThirdResolution() {
        Permanent seeker = addSeekerReady(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        activateAndResolve(seeker, target);
        activateAndResolve(seeker, target);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.addMana(player1, ManaColor.RED, 1);
        activateAndResolve(seeker, target);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(5);

        activateAndResolve(seeker, target);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability cannot target a creature controlled by an opponent")
    void cannotTargetOpponentsCreature() {
        Permanent seeker = addSeekerReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activateAndResolve(Permanent seeker, Permanent target) {
        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(seeker);
        harness.activateAbility(player1, sourceIndex, null, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addSeekerReady(Player player) {
        return addCreatureReady(player, new SoulbrightSeeker());
    }
}

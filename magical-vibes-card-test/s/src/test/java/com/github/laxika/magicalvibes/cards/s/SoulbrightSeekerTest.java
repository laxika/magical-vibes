package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulbrightSeeker.class, AirElemental.class, GrizzlyBears.class})
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

        harness.assertOnBattlefield(player1, "Soulbright Seeker");
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

        harness.assertInHand(player1, "Air Elemental");
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
        addSeekerReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPayAdditionalManaWithoutAnotherElemental() {
        harness.setHand(player1, List.of(new SoulbrightSeeker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Soulbright Seeker");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void beholdFromHandPubliclyRevealsTheOtherElementalBeforeResolution() {
        harness.setHand(player1, List.of(new SoulbrightSeeker(), new SoulbrightSeeker()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveals")
                && entry.plainText().contains("Soulbright Seeker"));
        harness.assertInHand(player1, "Soulbright Seeker");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Soulbright Seeker");
    }

    @Test
    void opponentsElementalDoesNotWaiveAdditionalMana() {
        harness.addToBattlefield(player2, new SoulbrightSeeker());
        harness.setHand(player1, List.of(new SoulbrightSeeker()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resolutionCountsAreSeparateForEachSeeker() {
        Permanent first = addSeekerReady(player1);
        Permanent second = addSeekerReady(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        activateAndResolve(first, first);
        activateAndResolve(first, first);
        activateAndResolve(second, second);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.addMana(player1, ManaColor.RED, 1);
        activateAndResolve(first, first);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
    }

    @Test
    void illegalTargetDoesNotCountAsAResolutionOrAwardMana() {
        Permanent seeker = addSeekerReady(player1);
        Permanent target = addSeekerReady(player1);
        harness.addMana(player1, ManaColor.RED, 4);
        activateAndResolve(seeker, seeker);
        activateAndResolve(seeker, seeker);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        activateAndResolve(seeker, seeker);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
    }

    @Test
    void thirdResolutionAwardsManaEvenAfterSourceLeavesBattlefield() {
        Permanent seeker = addSeekerReady(player1);
        Permanent target = addSeekerReady(player1);
        harness.addMana(player1, ManaColor.RED, 3);
        activateAndResolve(seeker, target);
        activateAndResolve(seeker, target);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(seeker);
        gd.playerGraveyards.get(player1.getId()).add(seeker.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void resolutionCountResetsEachTurn() {
        Permanent seeker = addSeekerReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        activateAndResolve(seeker, seeker);
        activateAndResolve(seeker, seeker);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 3);

        activateAndResolve(seeker, seeker);
        activateAndResolve(seeker, seeker);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        activateAndResolve(seeker, seeker);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
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

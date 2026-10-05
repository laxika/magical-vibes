package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EarthquakeDragon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanHellkite;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PatriarsHumiliation.class, GrizzlyBears.class, ShivanHellkite.class,
        EarthquakeDragon.class, Forest.class, Unsummon.class})
class PatriarsHumiliationTest extends BaseCardTest {

    @Test
    @DisplayName("Perpetually removes abilities and deals damage equal to your creatures")
    void removesAbilitiesAndDealsDamageEqualToControlledCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShivanHellkite());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castHumiliation(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();

        harness.addMana(player2, ManaColor.RED, 2);
        harness.ensurePriority(player2);
        assertThatThrownBy(() -> harness.activateAbility(player2, battlefieldIndex(player2, target), 0,
                null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void canTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castHumiliation(target);

        assertThat(target.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    void removesAbilitiesEvenWhenNoCreaturesAreControlled() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShivanHellkite());

        castHumiliation(target);

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    void canTargetOwnCreatureAndIncludesItInCreatureCount() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ShivanHellkite());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castHumiliation(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    void countsCreaturesAtResolutionAfterOneIsReturnedToHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShivanHellkite());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PatriarsHumiliation(), new Unsummon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, bear.getId());

        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    void perpetualAbilityLossSurvivesReturningToHandAndBeingRecast() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ShivanHellkite());
        castHumiliation(target);
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();
        harness.assertInHand(player1, "Shivan Hellkite");
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.ensurePriority(player1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Shivan Hellkite");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isFalse();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.ensurePriority(player1);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, returned),
                0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void perpetualAbilityLossPreventsGraveyardActivationAfterLethalDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EarthquakeDragon());
        harness.addToBattlefield(player2, new Forest());
        for (int i = 0; i < 10; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }

        castHumiliation(target);

        harness.assertInGraveyard(player2, "Earthquake Dragon");
        harness.assertNotOnBattlefield(player2, "Earthquake Dragon");
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.ensurePriority(player2);
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no graveyard activated ability");
    }

    private void castHumiliation(Permanent target) {
        harness.setHand(player1, List.of(new PatriarsHumiliation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}

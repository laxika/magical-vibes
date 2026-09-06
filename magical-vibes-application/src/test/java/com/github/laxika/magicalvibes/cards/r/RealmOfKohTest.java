package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InnocenceKami;
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

@CardUsed({RealmOfKoh.class, Forest.class, GrizzlyBears.class, InnocenceKami.class})
class RealmOfKohTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no basic land")
    void entersTappedWithoutBasicLand() {
        playRealm(player1);

        assertThat(findRealm(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a basic land")
    void entersUntappedWithBasicLand() {
        harness.addToBattlefield(player1, new Forest());

        playRealm(player1);

        assertThat(findRealm(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's basic land does not satisfy the check")
    void opponentBasicLandDoesNotSatisfyCheck() {
        harness.addToBattlefield(player2, new Forest());

        playRealm(player1);

        assertThat(findRealm(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for mana produces one black")
    void tappingProducesBlackMana() {
        Permanent realm = addReadyRealm(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(realm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability creates a colorless 1/1 Spirit token")
    void createsSpiritToken() {
        addReadyRealm(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit"))
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Spirit tokens can block and be blocked only by Spirit creatures")
    void spiritTokenCombatRestriction() {
        Permanent token = createSpiritToken();
        Permanent nonSpirit = addCreatureReady(player2, new GrizzlyBears());
        Permanent spirit = addCreatureReady(player2, new InnocenceKami());

        assertThat(bls.canBlockAttacker(gd, nonSpirit, token,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, spirit, token,
                gd.playerBattlefields.get(player2.getId()))).isTrue();

        Permanent nonSpiritAttacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent spiritAttacker = addCreatureReady(player2, new InnocenceKami());

        assertThat(bls.canBlockAttacker(gd, token, nonSpiritAttacker,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, token, spiritAttacker,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    private void playRealm(Player player) {
        harness.setHand(player, List.of(new RealmOfKoh()));
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player, 0);
    }

    private Permanent addReadyRealm(Player player) {
        Permanent realm = harness.addToBattlefieldAndReturn(player, new RealmOfKoh());
        realm.setSummoningSick(false);
        return realm;
    }

    private Permanent findRealm(Player player) {
        return findPermanent(player, "Realm of Koh");
    }

    private Permanent createSpiritToken() {
        addReadyRealm(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        return findPermanent(player1, "Spirit");
    }
}

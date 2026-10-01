package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSolifuge;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkeletalVampire.class, GiantSolifuge.class})
class SkeletalVampireTest extends BaseCardTest {

    @Test
    void enteringCreatesTwoFlyingBatTokens() {
        castSkeletalVampire();

        List<Permanent> bats = bats();
        assertThat(bats).hasSize(2);
        assertThat(bats).allSatisfy(bat -> {
            assertThat(bat.getCard().getPower()).isEqualTo(1);
            assertThat(bat.getCard().getToughness()).isEqualTo(1);
            assertThat(bat.getCard().getSubtypes()).containsExactly(CardSubtype.BAT);
            assertThat(bat.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    void sacrificingABatCreatesTwoReplacementBatTokens() {
        castSkeletalVampire();
        Permanent bat = bats().getFirst();

        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bat.getId());
        harness.passBothPriorities();

        assertThat(bats()).hasSize(3);
    }

    @Test
    void sacrificingABatGivesSkeletalVampireARegenerationShield() {
        castSkeletalVampire();
        Permanent vampire = findPermanent(player1, "Skeletal Vampire");
        Permanent bat = bats().getFirst();

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bat.getId());
        harness.passBothPriorities();

        assertThat(vampire.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void regenerationShieldSavesSkeletalVampireFromLethalCombatDamage() {
        castSkeletalVampire();
        Permanent vampire = findPermanent(player1, "Skeletal Vampire");
        Permanent bat = bats().getFirst();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, bat.getId());
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new GiantSolifuge());
        attacker.setAttacking(true);
        vampire.setBlocking(true);
        vampire.addBlockingTarget(0);

        resolveCombat(player2);
        harness.handleCombatDamageAssigned(player2, 0, Map.of(
                vampire.getId(), 3,
                player1.getId(), 1));

        harness.assertOnBattlefield(player1, "Skeletal Vampire");
        harness.assertInGraveyard(player2, "Giant Solifuge");
        assertThat(vampire.getRegenerationShield()).isZero();
        assertThat(vampire.isTapped()).isTrue();
        assertThat(vampire.getMarkedDamage()).isZero();
    }

    private void castSkeletalVampire() {
        harness.castFromHand(player1, new SkeletalVampire(), "{4}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Permanent> bats() {
        GameData gameData = harness.getGameData();
        return gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.BAT))
                .toList();
    }
}

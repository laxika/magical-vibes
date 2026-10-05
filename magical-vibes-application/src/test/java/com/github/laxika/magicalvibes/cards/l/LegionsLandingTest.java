package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({LegionsLanding.class, QueensBaySoldier.class})
class LegionsLandingTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 1/1 white Vampire token with lifelink")
    void etbCreatesVampireToken() {
        harness.setHand(player1, List.of(new LegionsLanding()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment, triggers ETB
        harness.passBothPriorities(); // resolve ETB trigger

        // Should have a Vampire token on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Vampire")
                        && p.getCard().isToken()
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1);
    }

    @Test
    @DisplayName("Transforms when attacking with exactly 3 creatures")
    void transformsWithThreeAttackers() {
        Permanent landing = addLandingReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        declareAttackers(List.of(1, 2, 3)); // indices 1,2,3 are the creatures (0 is Landing)
        harness.passBothPriorities(); // resolve transform trigger

        assertThat(landing.isTransformed()).isTrue();
        assertThat(landing.getCard().getName()).isEqualTo("Adanto, the First Fort");
    }

    @Test
    @DisplayName("Transforms when attacking with more than 3 creatures")
    void transformsWithFourAttackers() {
        Permanent landing = addLandingReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        declareAttackers(List.of(1, 2, 3, 4));
        harness.passBothPriorities(); // resolve transform trigger

        assertThat(landing.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform when attacking with only 2 creatures")
    void doesNotTransformWithTwoAttackers() {
        Permanent landing = addLandingReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        declareAttackers(List.of(1, 2));

        assertThat(landing.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Does not transform when attacking with only 1 creature")
    void doesNotTransformWithOneAttacker() {
        Permanent landing = addLandingReady(player1);
        addCreatureReady(player1);

        declareAttackers(List.of(1));

        assertThat(landing.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Adanto tap ability adds one white mana")
    void adantoTapAddsWhiteMana() {
        Permanent adanto = addTransformedAdanto(player1);

        int adantoIdx = indexOf(player1, adanto);
        harness.activateAbility(player1, adantoIdx, 0, null, null);
        // Mana ability resolves immediately (no stack) — don't pass priorities
        // which would advance steps and drain the mana pool

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Adanto activated ability creates a 1/1 Vampire token with lifelink")
    void adantoActivatedAbilityCreatesVampireToken() {
        Permanent adanto = addTransformedAdanto(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int adantoIdx = indexOf(player1, adanto);
        harness.activateAbility(player1, adantoIdx, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Vampire")
                        && p.getCard().isToken()
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1);
    }

    @Test
    @DisplayName("The entering Vampire has the specified characteristics and gains life in combat")
    void enteringVampireHasLifelink() {
        harness.setHand(player1, List.of(new LegionsLanding()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent vampire = findPermanent(player1, "Vampire");
        assertVampire(vampire);
        assertThat(countPermanents(player1, "Vampire")).isEqualTo(1);
        assertThat(countPermanents(player2, "Vampire")).isZero();
        vampire.setSummoningSick(false);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(indexOf(player1, vampire)));
        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Adanto pays three mana and taps to create the specified Vampire")
    void adantoTokenAbilityPaysCostsAndCreatesSpecifiedToken() {
        Permanent adanto = addTransformedAdanto(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(player1, adanto), 1, null, null);

        assertThat(adanto.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(countPermanents(player1, "Vampire")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Vampire")).isEqualTo(1);
        assertVampire(findPermanent(player1, "Vampire"));
    }

    @Test
    @DisplayName("Adanto's token ability cannot be activated without white mana")
    void adantoTokenAbilityRequiresWhiteMana() {
        Permanent adanto = addTransformedAdanto(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, adanto), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(adanto.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Vampire")).isZero();
    }

    @Test
    @DisplayName("An attack trigger still transforms Landing after an attacker leaves")
    void transformsAfterAttackerLeavesBeforeResolution() {
        Permanent landing = addLandingReady(player1);
        Permanent attacker = addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        declareAttackers(List.of(1, 2, 3));
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());

        harness.passBothPriorities();

        assertThat(landing.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("An opponent attacking with three creatures does not transform Landing")
    void opponentsAttackDoesNotTransformLanding() {
        Permanent landing = addLandingReady(player1);
        addCreatureReady(player2);
        addCreatureReady(player2);
        addCreatureReady(player2);

        declareAttackers(player2, List.of(0, 1, 2));

        assertThat(landing.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Transformation preserves tap status and does not create another entering token")
    void transformationPreservesTapStatusAndDoesNotRepeatEtb() {
        Permanent landing = addLandingReady(player1);
        landing.setTapped(true);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        declareAttackers(List.of(1, 2, 3));
        harness.passBothPriorities();

        assertThat(landing.isTransformed()).isTrue();
        assertThat(landing.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Vampire")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Newly transformed Adanto can immediately tap for white mana")
    void newlyTransformedAdantoCanTapForMana() {
        Permanent landing = addLandingReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        declareAttackers(List.of(1, 2, 3));
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, landing), 0, null, null);

        assertThat(landing.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void assertVampire(Permanent vampire) {
        assertThat(vampire.getCard().isToken()).isTrue();
        assertThat(vampire.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(vampire.getCard().getPower()).isEqualTo(1);
        assertThat(vampire.getCard().getToughness()).isEqualTo(1);
        assertThat(vampire.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(vampire.getCard().getSubtypes()).contains(CardSubtype.VAMPIRE);
        assertThat(vampire.getCard().getKeywords()).contains(Keyword.LIFELINK);
    }

    private Permanent addLandingReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new LegionsLanding());
    }

    private Permanent addTransformedAdanto(Player player) {
        Permanent perm = addLandingReady(player);
        perm.setCard(perm.getCard().getBackFaceCard());
        perm.setTransformed(true);
        return perm;
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new QueensBaySoldier());
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}

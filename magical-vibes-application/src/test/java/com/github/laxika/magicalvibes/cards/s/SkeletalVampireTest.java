package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSolifuge;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.cards.w.WingsOfVelisVel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkeletalVampire.class, GiantSolifuge.class, WingsOfVelisVel.class})
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

    @Test
    void createdBatsAreBlackCreatureTokens() {
        castSkeletalVampire();

        assertThat(bats()).hasSize(2).allSatisfy(bat -> {
            assertThat(bat.getCard().isToken()).isTrue();
            assertThat(bat.getCard().getColor()).isEqualTo(CardColor.BLACK);
        });
    }

    @Test
    void batIsSacrificedBeforeTokenCreationResolves() {
        castSkeletalVampire();
        Permanent bat = bats().getFirst();
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, bat.getId());

        assertThat(bats()).hasSize(1).doesNotContain(bat);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(bats()).hasSize(3);
    }

    @Test
    void tappedVampireCanRegenerateBySacrificingATappedBat() {
        castSkeletalVampire();
        Permanent vampire = findPermanent(player1, "Skeletal Vampire");
        Permanent bat = bats().getFirst();
        vampire.setTapped(true);
        bat.setTapped(true);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, bat.getId());
        assertThat(bats()).hasSize(1).doesNotContain(bat);
        assertThat(vampire.getRegenerationShield()).isZero();
        harness.passBothPriorities();

        assertThat(vampire.getRegenerationShield()).isEqualTo(1);
        assertThat(vampire.isTapped()).isTrue();
    }

    @Test
    void neitherAbilityCanBeActivatedWithoutABat() {
        harness.addToBattlefield(player1, new SkeletalVampire());
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificeChoicesExcludeNonBatsAndOpponentsBats() {
        castSkeletalVampire();
        addCreatureReady(player1, new GiantSolifuge());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new SkeletalVampire(), "{4}{B}{B}");
        resolveAllTriggers();
        List<Permanent> ownBats = bats();

        harness.activateAbility(player1, 0, 1, null, null);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds())
                .containsExactlyInAnyOrderElementsOf(ownBats.stream().map(Permanent::getId).toList());
        harness.handlePermanentChosen(player1, ownBats.getFirst().getId());
        harness.passBothPriorities();
    }

    @Test
    void vampireWithBatSubtypeCanSacrificeItselfToCreateTokens() {
        Permanent vampire = makeVampireABat();
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, vampire.getId());
        harness.assertInGraveyard(player1, "Skeletal Vampire");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skeletal Vampire");
        assertThat(bats()).hasSize(2);
    }

    @Test
    void vampireWithBatSubtypeCanSacrificeItselfForRegenerationButStaysDead() {
        Permanent vampire = makeVampireABat();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, vampire.getId());
        harness.assertInGraveyard(player1, "Skeletal Vampire");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skeletal Vampire");
        harness.assertInGraveyard(player1, "Skeletal Vampire");
        assertThat(bats()).isEmpty();
    }

    private Permanent makeVampireABat() {
        harness.addToBattlefield(player1, new SkeletalVampire());
        Permanent vampire = findPermanent(player1, "Skeletal Vampire");
        harness.setHand(player1, List.of(new WingsOfVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, vampire.getId());
        assertThat(gqs.hasEffectiveSubtype(gd, vampire, CardSubtype.BAT)).isTrue();
        return vampire;
    }

    private void castSkeletalVampire() {
        harness.castFromHand(player1, new SkeletalVampire(), "{4}{B}{B}");
        resolveAllTriggers();
    }

    private List<Permanent> bats() {
        return findPermanents(player1, "Bat");
    }
}

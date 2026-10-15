package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DouserOfLights;
import com.github.laxika.magicalvibes.cards.p.PauseForReflection;
import com.github.laxika.magicalvibes.cards.u.UnexplainedDisappearance;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmmaraSoulOfTheAccord.class, DouserOfLights.class,
        PauseForReflection.class, UnexplainedDisappearance.class})
class EmmaraSoulOfTheAccordTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Emmara creates a 1/1 white Soldier token with lifelink")
    void tappingEmmaraCreatesLifelinkSoldier() {
        Permanent emmara = harness.addToBattlefieldAndReturn(player1, new EmmaraSoulOfTheAccord());

        tap(emmara);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Soldier");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Tapping another creature you control does not trigger Emmara")
    void tappingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new EmmaraSoulOfTheAccord());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new DouserOfLights());

        tap(other);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attacking with Emmara creates one untapped Soldier that is not attacking")
    void attackingCreatesUntappedNonattackingSoldier() {
        Permanent emmara = harness.addToBattlefieldAndReturn(player1, new EmmaraSoulOfTheAccord());
        emmara.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList())
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
                    assertThat(token.isTapped()).isFalse();
                    assertThat(token.isAttacking()).isFalse();
                });
    }

    @Test
    @DisplayName("Emmara can convoke while summoning sick and creates a Soldier before the spell resolves")
    void convokingCreatesSoldierBeforeSpellResolves() {
        Permanent emmara = harness.addToBattlefieldAndReturn(player1, new EmmaraSoulOfTheAccord());
        emmara.setSummoningSick(true);
        harness.setHand(player1, List.of(new PauseForReflection()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(emmara.getId()));
        harness.passBothPriorities();

        assertThat(emmara.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList()).hasSize(1);
        assertThat(gd.preventAllCombatDamage).isFalse();

        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("Each tap after untapping Emmara creates another Soldier")
    void repeatedTapsCreateMultipleSoldiers() {
        Permanent emmara = harness.addToBattlefieldAndReturn(player1, new EmmaraSoulOfTheAccord());

        tap(emmara);
        harness.passBothPriorities();
        emmara.untap();
        tap(emmara);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList()).hasSize(2);
    }

    @Test
    @DisplayName("Tapping an opponent's creature does not trigger Emmara")
    void tappingOpponentsCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new EmmaraSoulOfTheAccord());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new DouserOfLights());

        tap(other);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning Emmara to hand in response does not stop her Soldier trigger")
    void triggerResolvesAfterEmmaraLeavesBattlefield() {
        Permanent emmara = harness.addToBattlefieldAndReturn(player1, new EmmaraSoulOfTheAccord());
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new UnexplainedDisappearance()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        tap(emmara);
        harness.castInstant(player2, 0, emmara.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Emmara, Soul of the Accord");
        harness.assertNotOnBattlefield(player1, "Emmara, Soul of the Accord");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}

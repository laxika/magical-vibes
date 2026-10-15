package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QuicksilverSpeedster;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MultiversalRecruitment.class, GrizzlyBears.class, QuicksilverSpeedster.class})
class MultiversalRecruitmentTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of a creature you control")
    void createsTokenCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castFromHand(target.getId());

        Permanent token = token();
        assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes legendary from the token copy")
    void removesLegendaryFromTokenCopy() {
        GrizzlyBears legendaryBears = new GrizzlyBears();
        legendaryBears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent target = harness.addToBattlefieldAndReturn(player1, legendaryBears);
        castFromHand(target.getId());

        assertThat(token().getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MultiversalRecruitment()));
        addManaForHandCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback creates the token and exiles the spell")
    void flashbackCreatesTokenAndExilesSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new MultiversalRecruitment()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(token()).isNotNull();
        harness.assertNotInGraveyard(player1, "Multiversal Recruitment");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Multiversal Recruitment"));
    }

    @Test
    @DisplayName("Copies a real legendary creature without copying counters or tapped status")
    void copiesLegendaryCreatureWithoutPermanentState() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new QuicksilverSpeedster());
        target.tap();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castFromHand(target.getId());

        Permanent copy = token();
        assertThat(copy.getCard().getName()).isEqualTo("Quicksilver, Speedster");
        assertThat(copy.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(copy.getEffectivePower()).isEqualTo(3);
        assertThat(copy.getEffectiveToughness()).isEqualTo(4);
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(copy.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target, copy);
        harness.assertInGraveyard(player1, "Multiversal Recruitment");
    }

    @Test
    @DisplayName("Does not copy a creature that is no longer controlled by the caster")
    void targetBecomesIllegalAfterControlChanges() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new QuicksilverSpeedster());
        harness.setHand(player1, List.of(new MultiversalRecruitment()));
        addManaForHandCast();
        harness.castSorcery(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
        harness.assertInGraveyard(player1, "Multiversal Recruitment");
    }

    @Test
    @DisplayName("Flashback exiles the spell even when its target has left the battlefield")
    void flashbackExilesSpellWithMissingTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new QuicksilverSpeedster());
        harness.setGraveyard(player1, List.of(new MultiversalRecruitment()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castFlashback(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Multiversal Recruitment");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Multiversal Recruitment"));
    }

    private void castFromHand(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new MultiversalRecruitment()));
        addManaForHandCast();
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    private void addManaForHandCast() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent token() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}

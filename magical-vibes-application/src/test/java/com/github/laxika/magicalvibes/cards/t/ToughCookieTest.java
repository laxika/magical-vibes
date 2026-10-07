package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CandyTrail;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ToughCookie.class, CandyTrail.class})
class ToughCookieTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when it enters")
    void createsFoodTokenOnEnter() {
        castToughCookie();

        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("The Food token can be sacrificed to gain 3 life")
    void foodCanBeSacrificedForLife() {
        castToughCookie();
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Food"));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, foodIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Animates a noncreature artifact you control as a 4/4 until end of turn")
    void animatesControlledNoncreatureArtifact() {
        harness.addToBattlefield(player1, new ToughCookie());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.isArtifact(artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target an artifact not controlled by its controller")
    void cannotTargetArtifactYouDoNotControl() {
        harness.addToBattlefield(player1, new ToughCookie());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CandyTrail());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Tough Cookie itself can be sacrificed for 3 life, paid before resolution")
    void cookieCanBeSacrificedForLife() {
        addCreatureReady(player1, new ToughCookie());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(countPermanents(player1, "Tough Cookie")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Cannot animate an artifact that is already a creature")
    void cannotTargetArtifactCreature() {
        Permanent cookie = harness.addToBattlefieldAndReturn(player1, new ToughCookie());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, cookie.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Food animation ends when the turn ends")
    void foodAnimationExpires() {
        castToughCookie();
        Permanent food = findPermanent(player1, "Food");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, food.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, food)).isTrue();
        assertThat(gqs.getEffectivePower(gd, food)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, food)).isEqualTo(4);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, food)).isFalse();
        assertThat(gqs.isArtifact(food)).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.activateAbility(player1, foodIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    private void castToughCookie() {
        harness.setHand(player1, List.of(new ToughCookie()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}

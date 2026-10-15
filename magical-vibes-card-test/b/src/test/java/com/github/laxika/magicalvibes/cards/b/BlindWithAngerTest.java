package com.github.laxika.magicalvibes.cards.b;

import java.util.List;

import com.github.laxika.magicalvibes.cards.c.ConsumingVortex;
import com.github.laxika.magicalvibes.cards.k.KondaLordOfEiganjo;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.s.SenseisDiviningTop;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlindWithAnger.class, ConsumingVortex.class, KondaLordOfEiganjo.class, LanternKami.class, SenseisDiviningTop.class})
class BlindWithAngerTest extends BaseCardTest {

    private void prepareSpell() {
        harness.setHand(player1, List.of(new BlindWithAnger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);
    }

    @Test
    @DisplayName("Untaps the target, steals it and gives it haste")
    void untapsStealsAndGrantsHaste() {
        Permanent target = addCreatureReady(player2, new LanternKami());
        target.tap();

        prepareSpell();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Control and haste wear off at end of turn")
    void controlAndHasteExpire() {
        Permanent target = addCreatureReady(player2, new LanternKami());

        prepareSpell();
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot target a legendary creature")
    void cannotTargetLegendaryCreature() {
        addCreatureReady(player1, new LanternKami()); // legal target so the spell is playable
        Permanent legendary = addCreatureReady(player2, new KondaLordOfEiganjo());

        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, legendary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonlegendary creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new LanternKami()); // legal target so the spell is playable
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SenseisDiviningTop());

        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonlegendary creature");
    }

    @Test
    @DisplayName("Can untap and grant haste to a creature already controlled by the caster")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LanternKami());
        target.tap();

        prepareSpell();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertOnBattlefield(player1, "Lantern Kami");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lantern Kami");
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not affect a creature that leaves and returns before resolution")
    void doesNotAffectReturnedCreature() {
        Permanent original = addCreatureReady(player2, new LanternKami());
        prepareSpell();
        harness.castInstant(player1, 0, original.getId());

        harness.setHand(player2, List.of(new ConsumingVortex()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, original.getId());
        harness.assertInHand(player2, "Lantern Kami");

        Permanent returned = harness.addToBattlefieldAndReturn(player2,
                gd.playerHands.get(player2.getId()).removeFirst());
        returned.tap();
        harness.passBothPriorities();

        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.hasKeyword(Keyword.HASTE)).isFalse();
        harness.assertOnBattlefield(player2, "Lantern Kami");
        harness.assertNotOnBattlefield(player1, "Lantern Kami");
        harness.assertInGraveyard(player1, "Blind with Anger");
        assertThat(gd.stack).isEmpty();
    }
}

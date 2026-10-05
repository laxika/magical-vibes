package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AgonyWarp;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.r.ResoundingRoar;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({Mosstodon.class, CylianElf.class, ResoundingRoar.class, AgonyWarp.class})
class MosstodonTest extends BaseCardTest {

    @Test
    @DisplayName("Grants trample to a target creature with power 5 or greater")
    void grantsTrampleToBigCreature() {
        addCreatureReady(player1, new Mosstodon());
        Permanent target = addCreatureReady(player1, new Mosstodon());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Trample wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new Mosstodon());
        Permanent target = addCreatureReady(player1, new Mosstodon());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Targeting a creature with power less than 5 is rejected")
    void illegalTargetRejected() {
        addCreatureReady(player1, new Mosstodon());
        Permanent bears = addCreatureReady(player1, new CylianElf());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetItselfWhileTapped() {
        Permanent source = addCreatureReady(player1, new Mosstodon());
        source.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, source.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.TRAMPLE)).isTrue();
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    void canTargetOpponentsCreature() {
        addCreatureReady(player1, new Mosstodon());
        Permanent target = addCreatureReady(player2, new Mosstodon());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void usesCurrentPowerIncludingTemporaryBoosts() {
        addCreatureReady(player1, new Mosstodon());
        Permanent target = addCreatureReady(player1, new CylianElf());
        harness.setHand(player1, List.of(new ResoundingRoar()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void targetBecomesIllegalWhenPowerDropsBeforeResolution() {
        addCreatureReady(player1, new Mosstodon());
        Permanent target = addCreatureReady(player1, new Mosstodon());
        Permanent other = addCreatureReady(player2, new CylianElf());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.setHand(player1, List.of(new AgonyWarp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, List.of(target.getId(), other.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterSourceDies() {
        Permanent source = addCreatureReady(player1, new Mosstodon());
        Permanent target = addCreatureReady(player1, new Mosstodon());
        Permanent other = addCreatureReady(player2, new CylianElf());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.setHand(player1, List.of(new AgonyWarp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, List.of(other.getId(), source.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }
}

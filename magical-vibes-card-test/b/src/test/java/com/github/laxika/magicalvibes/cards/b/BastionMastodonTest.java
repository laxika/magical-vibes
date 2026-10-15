package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BastionMastodon.class})
class BastionMastodonTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability grants Bastion Mastodon vigilance")
    void activationGrantsVigilance() {
        Permanent mastodon = addCreatureReady(player1, new BastionMastodon());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mastodon, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Granted vigilance wears off at end of turn")
    void vigilanceWearsOffAtEndOfTurn() {
        Permanent mastodon = addCreatureReady(player1, new BastionMastodon());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, mastodon, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mastodon, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Vigilance is granted on resolution only to the activating Mastodon")
    void vigilanceIsGrantedOnlyToSourceOnResolution() {
        Permanent source = addCreatureReady(player1, new BastionMastodon());
        Permanent other = addCreatureReady(player1, new BastionMastodon());
        Permanent opponent = addCreatureReady(player2, new BastionMastodon());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.hasKeyword(gd, source, Keyword.VIGILANCE)).isFalse();
        assertThat(source.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Granted vigilance lets Mastodon attack without tapping")
    void attacksWithoutTappingAfterActivation() {
        Permanent mastodon = addCreatureReady(player1, new BastionMastodon());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(mastodon.isAttacking()).isTrue();
        assertThat(mastodon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapped summoning-sick Mastodon can activate and remains tapped")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent mastodon = harness.addToBattlefieldAndReturn(player1, new BastionMastodon());
        mastodon.setSummoningSick(true);
        mastodon.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mastodon, Keyword.VIGILANCE)).isTrue();
        assertThat(mastodon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Colorless mana cannot pay the white activation cost")
    void cannotActivateWithOnlyColorlessMana() {
        Permanent mastodon = addCreatureReady(player1, new BastionMastodon());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gqs.hasKeyword(gd, mastodon, Keyword.VIGILANCE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}

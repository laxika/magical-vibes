package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodcrazedSocialite.class})
class BloodcrazedSocialiteTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Blood token")
    void entersWithBloodToken() {
        Permanent socialite = castSocialite();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(socialite)
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("May sacrifice a Blood token when it attacks to get +2/+2")
    void sacrificingBloodTokenBoostsSocialite() {
        Permanent socialite = castReadySocialite();
        Permanent blood = findBloodToken();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(socialite)));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blood.getId());

        assertThat(gqs.getEffectivePower(gd, socialite)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, socialite)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blood);
    }

    @Test
    @DisplayName("Declining the Blood sacrifice does not boost it")
    void decliningSacrificeDoesNotBoostSocialite() {
        Permanent socialite = castReadySocialite();
        Permanent blood = findBloodToken();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(socialite)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, socialite)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, socialite)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blood);
    }

    private Permanent castReadySocialite() {
        Permanent socialite = castSocialite();
        socialite.setSummoningSick(false);
        return socialite;
    }

    private Permanent castSocialite() {
        Card card = new BloodcrazedSocialite();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }

    private Permanent findBloodToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}

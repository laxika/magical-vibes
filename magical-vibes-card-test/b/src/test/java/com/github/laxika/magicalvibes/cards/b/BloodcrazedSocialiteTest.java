package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SculptingSteel;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodcrazedSocialite.class, SculptingSteel.class})
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

    @Test
    @DisplayName("Menace requires at least two blockers")
    void menaceRequiresTwoBlockers() {
        addCreatureReady(player1, new BloodcrazedSocialite());
        addCreatureReady(player2, new BloodcrazedSocialite());
        addCreatureReady(player2, new BloodcrazedSocialite());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, false);
        }
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
    }

    @Test
    @DisplayName("Attacking without Blood does not boost it or require a sacrifice choice")
    void attackingWithoutBloodDoesNotBoostSocialite() {
        Permanent socialite = addCreatureReady(player1, new BloodcrazedSocialite());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gqs.getEffectivePower(gd, socialite)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, socialite)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's Blood cannot be sacrificed")
    void cannotSacrificeOpponentsBlood() {
        Permanent socialite = addCreatureReady(player1, new BloodcrazedSocialite());
        harness.enterBattlefieldAndReturn(player2, new BloodcrazedSocialite());
        resolveAllTriggers();
        Permanent blood = findPermanent(player2, "Blood");

        declareAttackers(List.of(0));
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blood);
        assertThat(gqs.getEffectivePower(gd, socialite)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, socialite)).isEqualTo(3);
    }

    @Test
    @DisplayName("The sacrifice boost expires at end of turn")
    void sacrificeBoostExpires() {
        Permanent socialite = castReadySocialite();
        Permanent blood = findBloodToken();

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blood.getId());
        assertThat(gqs.getEffectivePower(gd, socialite)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, socialite)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, socialite)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, socialite)).isEqualTo(3);
    }

    @Test
    @CardUsed({SculptingSteel.class})
    @DisplayName("A nontoken copy of Blood is not an eligible sacrifice")
    void cannotSacrificeNontokenBloodCopy() {
        Permanent socialite = castReadySocialite();
        Permanent blood = findBloodToken();
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blood.getId());
        Permanent steel = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof SculptingSteel)
                .findFirst().orElseThrow();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(socialite)));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(blood.getId());
        harness.handlePermanentChosen(player1, blood.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(steel).doesNotContain(blood);
        assertThat(gqs.getEffectivePower(gd, socialite)).isEqualTo(5);
    }

    private Permanent castReadySocialite() {
        Permanent socialite = castSocialite();
        socialite.setSummoningSick(false);
        return socialite;
    }

    private Permanent castSocialite() {
        harness.setHand(player1, List.of(new BloodcrazedSocialite()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        return findPermanent(player1, "Bloodcrazed Socialite");
    }

    private Permanent findBloodToken() {
        return findPermanent(player1, "Blood");
    }
}

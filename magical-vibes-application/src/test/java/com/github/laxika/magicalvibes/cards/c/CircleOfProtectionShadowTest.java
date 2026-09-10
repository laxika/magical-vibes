package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DauthiEmbrace;
import com.github.laxika.magicalvibes.cards.f.Fireslinger;
import com.github.laxika.magicalvibes.cards.t.ThalakosSentry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CircleOfProtectionShadow.class, CanopySpider.class, DauthiEmbrace.class, Fireslinger.class,
        ThalakosSentry.class})
class CircleOfProtectionShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Only creatures with shadow are offered as prevention sources")
    void onlyShadowCreaturesAreValidSources() {
        addReadyCircle(player1);
        Permanent shadowCreature = addReadyShadowCreature(player2);
        Permanent normalCreature = addCreatureReady(player2, new CanopySpider());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(shadowCreature.getId())
                .doesNotContain(normalCreature.getId());
    }

    @Test
    @DisplayName("Does not offer a source when no creature with shadow exists")
    void noShadowCreatureMeansNoSourceChoice() {
        addReadyCircle(player1);
        addCreatureReady(player2, new CanopySpider());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
        assertThat(gameLogContains("No permanents on the battlefield")).isTrue();
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen shadow creature")
    void preventsNextDamageFromChosenShadowCreature() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent shadowCreature = addReadyShadowCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, shadowCreature.getId());

        shadowCreature.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents noncombat damage from a creature with shadow")
    void preventsNextNoncombatDamageFromShadowCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addReadyCircle(player1);
        Permanent fireslinger = addCreatureReady(player2, new Fireslinger());
        Permanent dauthiEmbrace = harness.addToBattlefieldAndReturn(player2, new DauthiEmbrace());

        harness.addMana(player2, ManaColor.BLACK, 2);
        int dauthiEmbraceIndex = gd.playerBattlefields.get(player2.getId()).indexOf(dauthiEmbrace);
        harness.activateAbility(player2, dauthiEmbraceIndex, null, fireslinger.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(fireslinger.getId());

        harness.handlePermanentChosen(player1, fireslinger.getId());
        int fireslingerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(fireslinger);
        harness.activateAbility(player2, fireslingerIndex, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private Permanent addReadyCircle(Player player) {
        return addCreatureReady(player, new CircleOfProtectionShadow());
    }

    private Permanent addReadyShadowCreature(Player player) {
        return addCreatureReady(player, new ThalakosSentry());
    }
}

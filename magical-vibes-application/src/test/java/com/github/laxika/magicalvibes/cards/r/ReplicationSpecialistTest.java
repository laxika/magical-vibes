package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GlintHawkIdol;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReplicationSpecialist.class, GlintHawkIdol.class, GrizzlyBears.class})
class ReplicationSpecialistTest extends BaseCardTest {

    @Test
    void payingTheTriggerCreatesATokenCopyOfTheArtifact() {
        addSpecialistReady(player1);
        harness.setHand(player1, List.of(new GlintHawkIdol()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Glint Hawk Idol")).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Glint Hawk Idol") && p.getCard().isToken())
                .count()).isEqualTo(1);
    }

    @Test
    void decliningTheTriggerDoesNotCreateAToken() {
        addSpecialistReady(player1);
        harness.setHand(player1, List.of(new GlintHawkIdol()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Glint Hawk Idol")).isEqualTo(1);
    }

    @Test
    void cannotPayTheTriggerWithoutBlueMana() {
        addSpecialistReady(player1);
        harness.setHand(player1, List.of(new GlintHawkIdol()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Glint Hawk Idol")).isEqualTo(1);
    }

    @Test
    void tokenCopyDoesNotRetriggerTheAbility() {
        addSpecialistReady(player1);
        harness.setHand(player1, List.of(new GlintHawkIdol()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void nonArtifactDoesNotTriggerTheAbility() {
        addSpecialistReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    private Permanent addSpecialistReady(Player player) {
        Permanent permanent = new Permanent(new ReplicationSpecialist());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}

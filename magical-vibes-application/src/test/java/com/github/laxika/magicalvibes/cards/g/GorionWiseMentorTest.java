package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HeartsDesire;
import com.github.laxika.magicalvibes.cards.b.BonecrusherGiant;
import com.github.laxika.magicalvibes.cards.l.LovestruckBeast;
import com.github.laxika.magicalvibes.cards.s.Stomp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GorionWiseMentor.class, HeartsDesire.class, LovestruckBeast.class,
        BonecrusherGiant.class, Stomp.class})
class GorionWiseMentorTest extends BaseCardTest {

    @Test
    void acceptingTheOptionalCopyCopiesAnAdventureSpell() {
        harness.addToBattlefield(player1, new GorionWiseMentor());
        harness.setHand(player1, List.of(new LovestruckBeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, List.of());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Human")).isEqualTo(2);
    }

    @Test
    void decliningDoesNotCopyTheAdventureSpell() {
        harness.addToBattlefield(player1, new GorionWiseMentor());
        harness.setHand(player1, List.of(new LovestruckBeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, List.of());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Human")).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForNormalCreatureSpell() {
        harness.addToBattlefield(player1, new GorionWiseMentor());
        harness.setHand(player1, List.of(new LovestruckBeast()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void copyChoiceIsMadeOnlyWhenTheTriggeredAbilityResolves() {
        harness.addToBattlefield(player1, new GorionWiseMentor());
        harness.setHand(player1, List.of(new LovestruckBeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Human")).isEqualTo(1);
    }

    @Test
    void opponentsAdventureDoesNotTriggerGorion() {
        harness.addToBattlefield(player2, new GorionWiseMentor());
        harness.setHand(player1, List.of(new LovestruckBeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Human")).isEqualTo(1);
        assertThat(countPermanents(player2, "Human")).isZero();
    }

    @Test
    void copyCanKeepTheOriginalTarget() {
        harness.addToBattlefield(player1, new GorionWiseMentor());
        harness.setHand(player1, List.of(new BonecrusherGiant()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAdventure(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
    }

    @Test
    void copyCanChooseANewTargetWithoutChangingTheOriginal() {
        harness.addToBattlefield(player1, new GorionWiseMentor());
        BonecrusherGiant adventureCard = new BonecrusherGiant();
        harness.setHand(player1, List.of(adventureCard));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAdventure(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        assertThat(gd.findExiledCard(adventureCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsOnlyKeys(adventureCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}

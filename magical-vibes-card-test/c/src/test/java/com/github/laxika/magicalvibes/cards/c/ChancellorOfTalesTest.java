package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartsDesire;
import com.github.laxika.magicalvibes.cards.l.LovestruckBeast;
import com.github.laxika.magicalvibes.cards.e.EmberethBlaze;
import com.github.laxika.magicalvibes.cards.v.VirtueOfCourage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChancellorOfTales.class, GrizzlyBears.class, HeartsDesire.class, LovestruckBeast.class,
        VirtueOfCourage.class, EmberethBlaze.class})
class ChancellorOfTalesTest extends BaseCardTest {

    @Test
    void acceptingTheOptionalCopyCopiesAnAdventureSpell() {
        harness.addToBattlefield(player1, new ChancellorOfTales());
        var adventureCard = new LovestruckBeast();
        harness.setHand(player1, List.of(adventureCard));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, List.of());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Human")).isEqualTo(2);
        assertThat(gd.exiledCards).hasSize(1);
        assertThat(gd.findExiledCard(adventureCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(adventureCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    void decliningDoesNotCopyTheAdventureSpell() {
        harness.addToBattlefield(player1, new ChancellorOfTales());
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
        harness.addToBattlefield(player1, new ChancellorOfTales());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void optionalCopyChoiceIsMadeOnlyWhenTheTriggerResolves() {
        harness.addToBattlefield(player1, new ChancellorOfTales());
        harness.setHand(player1, List.of(new VirtueOfCourage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAdventure(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    void mayChooseANewTargetForTheAdventureCopy() {
        harness.addToBattlefield(player1, new ChancellorOfTales());
        harness.setHand(player1, List.of(new VirtueOfCourage()));
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
    }

    @Test
    void decliningNewTargetsKeepsTheOriginalTarget() {
        harness.addToBattlefield(player1, new ChancellorOfTales());
        harness.setHand(player1, List.of(new VirtueOfCourage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAdventure(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }

    @Test
    void doesNotTriggerForAnOpponentsAdventure() {
        harness.addToBattlefield(player1, new ChancellorOfTales());
        harness.setHand(player2, List.of(new VirtueOfCourage()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAdventure(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertLife(player1, 18);
    }

    @Test
    void doesNotTriggerWhenAnAdventureCardIsCastAsAPermanent() {
        harness.addToBattlefield(player1, new ChancellorOfTales());
        harness.setHand(player1, List.of(new VirtueOfCourage()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Virtue of Courage");
    }
}

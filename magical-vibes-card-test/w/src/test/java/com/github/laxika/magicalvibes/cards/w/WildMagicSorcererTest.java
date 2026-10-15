package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.Cultivate;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.v.VeyranVoiceOfDuality;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildMagicSorcerer.class, GrizzlyBears.class, Divination.class, HillGiant.class,
        LlanowarElves.class, Cultivate.class, VeyranVoiceOfDuality.class})
class WildMagicSorcererTest extends BaseCardTest {

    @Test
    void firstSpellCastFromExileGetsCascade() {
        setupSorcerer();
        Card exiledSpell = new HillGiant();
        harness.setExile(player1, List.of(exiledSpell));
        gd.exilePlayPermissions.put(exiledSpell.getId(), player1.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromExile(player1, exiledSpell.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).extracting("name").containsExactly("Grizzly Bears");
    }

    @Test
    void cascadeUsesTheExiledSpellsManaValue() {
        setupSorcerer();
        Card exiledSpell = new GrizzlyBears();
        harness.setExile(player1, List.of(exiledSpell));
        gd.exilePlayPermissions.put(exiledSpell.getId(), player1.getId());
        harness.setLibrary(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, exiledSpell.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void handSpellDoesNotGetCascade() {
        setupSorcerer();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void handSpellDoesNotConsumeTheFirstExileSpellTrigger() {
        setupSorcerer();
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Card exiledSpell = new GrizzlyBears();
        harness.setExile(player1, List.of(exiledSpell));
        gd.exilePlayPermissions.put(exiledSpell.getId(), player1.getId());
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.castFromExile(player1, exiledSpell.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).extracting("name").containsExactly("Llanowar Elves");
    }

    @Test
    void veyranDoesNotDoubleTheSpellsGrantedCascade() {
        setupSorcerer();
        harness.addToBattlefield(player1, new VeyranVoiceOfDuality());
        Card exiledSpell = new Cultivate();
        harness.setExile(player1, List.of(exiledSpell));
        gd.exilePlayPermissions.put(exiledSpell.getId(), player1.getId());
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromExile(player1, exiledSpell.getId());

        // Cultivate, two magecraft triggers, and one cascade trigger.
        assertThat(gd.stack).hasSize(4);
    }

    @Test
    void secondSpellFromExileDoesNotCascade() {
        setupSorcerer();
        Card firstSpell = new LlanowarElves();
        Card secondSpell = new WildMagicSorcerer();
        harness.setExile(player1, List.of(firstSpell, secondSpell));
        gd.exilePlayPermissions.put(firstSpell.getId(), player1.getId());
        gd.exilePlayPermissions.put(secondSpell.getId(), player1.getId());
        harness.setLibrary(player1, List.of(new WildMagicSorcerer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromExile(player1, firstSpell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castFromExile(player1, secondSpell.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Wild-Magic Sorcerer")).isEqualTo(2);
    }

    @Test
    void spellCastBeforeSorcererEntersConsumesTheFirstExileSpell() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        Card firstSpell = new LlanowarElves();
        Card secondSpell = new WildMagicSorcerer();
        harness.setExile(player1, List.of(firstSpell, secondSpell));
        gd.exilePlayPermissions.put(firstSpell.getId(), player1.getId());
        gd.exilePlayPermissions.put(secondSpell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, firstSpell.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new WildMagicSorcerer());
        harness.setLibrary(player1, List.of(new LlanowarElves()));

        harness.castFromExile(player1, secondSpell.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void eachSorcererGrantsOneCascadeAndDecliningReturnsTheCard() {
        setupSorcerer();
        harness.addToBattlefield(player1, new WildMagicSorcerer());
        Card exiledSpell = new WildMagicSorcerer();
        Card hit = new LlanowarElves();
        harness.setExile(player1, List.of(exiledSpell));
        gd.exilePlayPermissions.put(exiledSpell.getId(), player1.getId());
        harness.setLibrary(player1, List.of(hit));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromExile(player1, exiledSpell.getId());
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(hit);
        assertThat(gd.findExiledCard(hit.getId())).isNull();
        assertThat(countPermanents(player1, "Wild-Magic Sorcerer")).isEqualTo(3);
    }

    @Test
    void opponentsSorcererDoesNotGrantCascade() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player2, new WildMagicSorcerer());
        Card exiledSpell = new WildMagicSorcerer();
        harness.setExile(player1, List.of(exiledSpell));
        gd.exilePlayPermissions.put(exiledSpell.getId(), player1.getId());
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromExile(player1, exiledSpell.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cascadeCastsTheHitForFreeWithoutCascadingAgain() {
        setupSorcerer();
        Card exiledSpell = new WildMagicSorcerer();
        Card hit = new LlanowarElves();
        Card remainingCard = new Cultivate();
        harness.setExile(player1, List.of(exiledSpell));
        gd.exilePlayPermissions.put(exiledSpell.getId(), player1.getId());
        harness.setLibrary(player1, List.of(hit, remainingCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromExile(player1, exiledSpell.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(countPermanents(player1, "Wild-Magic Sorcerer")).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Wild-Magic Sorcerer")).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.findExiledCard(hit.getId())).isNull();
    }

    @Test
    void firstExileSpellRestrictionResetsOnANewTurn() {
        setupSorcerer();
        Card firstSpell = new LlanowarElves();
        Card nextTurnSpell = new WildMagicSorcerer();
        harness.setExile(player1, List.of(firstSpell, nextTurnSpell));
        gd.exilePlayPermissions.put(firstSpell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, List.of(new WildMagicSorcerer(), new WildMagicSorcerer()));
        harness.setLibrary(player2, List.of(new WildMagicSorcerer(), new WildMagicSorcerer()));
        harness.castFromExile(player1, firstSpell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        gd.exilePlayPermissions.put(nextTurnSpell.getId(), player1.getId());
        Card hit = new LlanowarElves();
        harness.setLibrary(player1, List.of(hit));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, nextTurnSpell.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
    }

    private void setupSorcerer() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new WildMagicSorcerer());
    }
}

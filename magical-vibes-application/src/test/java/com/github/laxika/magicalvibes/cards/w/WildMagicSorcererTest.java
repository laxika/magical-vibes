package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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
        LlanowarElves.class})
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

    private void setupSorcerer() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new WildMagicSorcerer());
    }
}

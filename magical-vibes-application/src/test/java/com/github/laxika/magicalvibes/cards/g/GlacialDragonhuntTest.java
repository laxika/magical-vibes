package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlacialDragonhunt.class, Forest.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class GlacialDragonhuntTest extends BaseCardTest {

    @Test
    void drawsThenDiscardingANonlandDealsThreeDamageToTargetCreature() {
        harness.setHand(player1, List.of(new GlacialDragonhunt()));
        harness.setLibrary(player1, List.of(new Shock()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void discardingALandDoesNotDealDamage() {
        harness.setHand(player1, List.of(new GlacialDragonhunt()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void mayDeclineToDiscard() {
        harness.setHand(player1, List.of(new GlacialDragonhunt()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void harmonizeTapsACreatureReducesGenericCostAndExilesTheSpell() {
        Card spell = new GlacialDragonhunt();
        Permanent reducer = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(spell));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashbackWithTapCost(player1, 0, List.of(reducer.getId()));
        assertThat(reducer.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}

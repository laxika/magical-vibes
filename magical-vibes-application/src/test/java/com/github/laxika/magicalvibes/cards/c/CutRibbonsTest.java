package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.f.Flashfreeze;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CutRibbons.class, DuneBeetle.class, Colossapede.class})
class CutRibbonsTest extends BaseCardTest {

    @Test
    @DisplayName("Cut deals 4 damage to target creature")
    void cutDealsFourToCreature() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        harness.setHand(player1, List.of(new CutRibbons()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, beetle.getId());

        harness.assertNotOnBattlefield(player2, "Dune Beetle");
        harness.assertInGraveyard(player1, "Cut");
    }

    @Test
    @DisplayName("Cut rejects a player as target")
    void cutRejectsPlayerTarget() {
        harness.setHand(player1, List.of(new CutRibbons()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ribbons from graveyard makes each opponent lose X life then exiles")
    void ribbonsLosesXLifeThenExiles() {
        harness.setGraveyard(player1, List.of(new CutRibbons()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player2, 20);

        harness.castFlashback(player1, 0, 3, (UUID) null);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
        harness.assertNotInGraveyard(player1, "Cut");
        harness.assertNotInGraveyard(player1, "Ribbons");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cut"));
    }

    @Test
    @DisplayName("Ribbons with X=0 does nothing then exiles")
    void ribbonsXZeroExiles() {
        harness.setGraveyard(player1, List.of(new CutRibbons()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player2, 20);

        harness.castFlashback(player1, 0, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cut"));
    }

    @Test
    @DisplayName("Cut deals exactly four damage and can target a creature you control")
    void cutMarksExactlyFourDamageOnOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        harness.setHand(player1, List.of(new CutRibbons()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Colossapede");
        assertThat(creature.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Cut");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The same card can cast Cut from hand then Ribbons from the graveyard")
    void cutThenRibbonsUsesBothHalves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        CutRibbons card = new CutRibbons();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());
        harness.assertInGraveyard(player1, "Cut");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFlashback(player1, 0, 4, (UUID) null);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
        harness.assertNotInGraveyard(player1, "Cut");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Ribbons cannot be cast outside a main phase")
    void ribbonsRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new CutRibbons()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        gd.currentStep = TurnStep.COMBAT_DAMAGE;

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Cut");
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({Flashfreeze.class})
    @DisplayName("Ribbons is black on the stack and cannot be targeted by Flashfreeze")
    void ribbonsCannotBeTargetedByFlashfreeze() {
        harness.setGraveyard(player1, List.of(new CutRibbons()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, 3, (UUID) null);
        UUID spellId = gd.stack.getFirst().getCard().getId();
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, spellId))
                .isInstanceOf(IllegalStateException.class);
    }
}

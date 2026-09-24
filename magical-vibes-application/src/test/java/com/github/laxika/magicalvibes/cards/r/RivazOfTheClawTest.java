package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.p.Prismite;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RivazOfTheClaw.class, DragonWhelp.class, Prismite.class, LightningStrike.class})
class RivazOfTheClawTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Rivaz adds two independently chosen Dragon creature mana")
    void addsTwoDragonCreatureMana() {
        Permanent rivaz = addCreatureReady(player1, new RivazOfTheClaw());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(rivaz.isTapped()).isTrue();
        assertThat(pool.getSubtypeCreatureManaForColor(Set.of(CardSubtype.DRAGON), ManaColor.RED))
                .isEqualTo(1);
        assertThat(pool.getSubtypeCreatureManaForColor(Set.of(CardSubtype.DRAGON), ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Rivaz's mana can cast a Dragon creature but not another creature")
    void manaIsRestrictedToDragonCreatures() {
        addCreatureReady(player1, new RivazOfTheClaw());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.RED.name());

        harness.setHand(player1, List.of(new Prismite()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new DragonWhelp()));
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Rivaz permits only one Dragon creature cast from the graveyard each turn")
    void castsOneDragonFromGraveyardPerTurn() {
        harness.addToBattlefield(player1, new RivazOfTheClaw());
        DragonWhelp first = new DragonWhelp();
        DragonWhelp second = new DragonWhelp();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 6);
        prepareMainPhase();

        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Dragon cast from the graveyard is exiled when it dies")
    void exilesDragonWhenItDies() {
        harness.addToBattlefield(player1, new RivazOfTheClaw());
        DragonWhelp dragon = new DragonWhelp();
        harness.setGraveyard(player1, List.of(dragon));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 4);
        prepareMainPhase();

        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();

        Permanent dragonPermanent = findPermanent(player1, "Dragon Whelp");
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, dragonPermanent.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Dragon Whelp");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .doesNotContain("Dragon Whelp");
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}

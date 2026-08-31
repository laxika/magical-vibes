package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.p.Pillage;
import com.github.laxika.magicalvibes.cards.t.TheWaterCrystal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HelmOfObedience.class, Pillage.class, DeadlyInsect.class})
class HelmOfObedienceTest extends BaseCardTest {

    @Test
    @DisplayName("Stops at the first milled creature, sacrifices itself and reanimates it under your control")
    void millsUntilCreatureAndReanimates() {
        harness.addToBattlefield(player1, new HelmOfObedience());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        Card creature = new DeadlyInsect();
        Card remaining = new Pillage();
        harness.setLibrary(player2, List.of(new Pillage(), creature, remaining));

        harness.activateAbility(player1, 0, 5, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Deadly Insect")).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName).contains("Helm of Obedience");
    }

    @Test
    @DisplayName("Milling X non-creature cards leaves the Helm on the battlefield")
    void noCreatureFoundKeepsHelm() {
        harness.addToBattlefield(player1, new HelmOfObedience());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card creature = new DeadlyInsect();
        harness.setLibrary(player2, List.of(new Pillage(), new Pillage(), creature));

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(creature);
        assertThat(findPermanent(player1, "Helm of Obedience")).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Deadly Insect"));
    }

    @Test
    @DisplayName("An empty library ends the process with no reanimation")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new HelmOfObedience());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(findPermanent(player1, "Helm of Obedience")).isNotNull();
    }

    @Test
    @DisplayName("Rejects an activation with X equal to zero")
    void cannotActivateWithZeroX() {
        harness.addToBattlefield(player1, new HelmOfObedience());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X");
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetController() {
        harness.addToBattlefield(player1, new HelmOfObedience());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @CardUsed(LeylineOfTheVoid.class)
    @DisplayName("Keeps milling when a replacement effect prevents cards from reaching the graveyard")
    void replacementEffectDoesNotCountAsCardsPutIntoGraveyard() {
        harness.addToBattlefield(player1, new HelmOfObedience());
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Card first = new Pillage();
        Card creature = new DeadlyInsect();
        Card last = new Pillage();
        harness.setLibrary(player2, List.of(first, creature, last));

        harness.activateAbility(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, creature, last);
        assertThat(findPermanent(player1, "Helm of Obedience")).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Deadly Insect"));
    }

    @Test
    @CardUsed(TheWaterCrystal.class)
    @DisplayName("Recognizes a creature among cards added by a mill replacement effect")
    void findsCreatureAmongAdditionalMilledCards() {
        harness.addToBattlefield(player1, new HelmOfObedience());
        harness.addToBattlefield(player1, new TheWaterCrystal());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card creature = new DeadlyInsect();
        harness.setLibrary(player2, List.of(
                new Pillage(), creature, new Pillage(), new Pillage(), new Pillage()));

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(findPermanent(player1, "Deadly Insect")).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName).contains("Helm of Obedience");
    }
}

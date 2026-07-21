package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EtheriumAstrolabeTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {B}, tapping, and sacrificing an artifact draws a card")
    void drawsCardOnResolution() {
        harness.addToBattlefield(player1, new EtheriumAstrolabe());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        java.util.UUID spellbookId = findPermanent(player1, "Spellbook").getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbookId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
    }

    @Test
    @DisplayName("Can sacrifice itself to pay the ability and still draws")
    void canSacrificeItselfToDraw() {
        harness.addToBattlefield(player1, new EtheriumAstrolabe());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Etherium Astrolabe"));
    }

    @Test
    @DisplayName("Cannot activate the ability without paying the {B} mana cost")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new EtheriumAstrolabe());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating taps Etherium Astrolabe when sacrificing another artifact")
    void tapsOnActivation() {
        harness.addToBattlefield(player1, new EtheriumAstrolabe());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        java.util.UUID spellbookId = findPermanent(player1, "Spellbook").getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbookId);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Etherium Astrolabe").isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new EtheriumAstrolabe());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.BLACK, 1);
        findPermanent(player1, "Etherium Astrolabe").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Noncreature artifact can activate while summoning sick")
    void canActivateWhileSummoningSick() {
        Permanent astrolabe = new Permanent(new EtheriumAstrolabe());
        astrolabe.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(astrolabe);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Etherium Astrolabe"));
    }
}

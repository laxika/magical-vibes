package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulreaperOfMogis.class, Forest.class, NyxbornColossus.class})
class SoulreaperOfMogisTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices the chosen creature and puts the ability on the stack")
    void activatingSacrificesChosenCreature() {
        addSoulreaper(player1);
        Permanent sacrifice = addCreature(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        harness.assertNotOnBattlefield(player1, "Nyxborn Colossus");
        harness.assertInGraveyard(player1, "Nyxborn Colossus");
        harness.assertOnBattlefield(player1, "Soulreaper of Mogis");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving the ability draws a card")
    void resolvingDrawsACard() {
        addSoulreaper(player1);
        Permanent sacrifice = addCreature(player1);
        addAbilityMana(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
    }

    @Test
    @DisplayName("The source creature may be sacrificed to its own ability")
    void canSacrificeItself() {
        addSoulreaper(player1);
        addAbilityMana(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Soulreaper of Mogis");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addSoulreaper(player1);
        addCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Soulreaper can sacrifice itself")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent soulreaper = harness.addToBattlefieldAndReturn(player1, new SoulreaperOfMogis());
        soulreaper.setSummoningSick(true);
        soulreaper.tap();
        addAbilityMana(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Soulreaper of Mogis");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        addSoulreaper(player1);
        Permanent sacrifice = addSoulreaper(player1);
        Permanent opponentCreature = addSoulreaper(player2);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.assertInGraveyard(player1, "Soulreaper of Mogis");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A noncreature permanent cannot pay the sacrifice cost")
    void cannotSacrificeLand() {
        addSoulreaper(player1);
        Permanent sacrifice = addSoulreaper(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Forest");
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.assertInGraveyard(player1, "Soulreaper of Mogis");
        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addSoulreaper(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SoulreaperOfMogis());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new NyxbornColossus());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

}

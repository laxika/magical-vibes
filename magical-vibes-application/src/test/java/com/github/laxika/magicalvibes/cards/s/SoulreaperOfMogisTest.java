package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SoulreaperOfMogis.class, Forest.class, GrizzlyBears.class})
class SoulreaperOfMogisTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices the chosen creature and puts the ability on the stack")
    void activatingSacrificesChosenCreature() {
        addSoulreaper(player1);
        Permanent sacrifice = addCreature(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
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
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
    }

    @Test
    @DisplayName("The source creature may be sacrificed to its own ability")
    void canSacrificeItself() {
        Permanent soulreaper = addSoulreaper(player1);
        addAbilityMana(player1);
        harness.setHand(player1, List.of());
        setDeck(player1, List.of(new Forest()));

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

    private Permanent addSoulreaper(Player player) {
        Permanent permanent = new Permanent(new SoulreaperOfMogis());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private void setDeck(Player player, List<? extends Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}

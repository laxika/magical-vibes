package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShiftingShadow.class, GrizzlyBears.class, Forest.class})
class ShiftingShadowTest extends BaseCardTest {

    private Permanent attachShadow(Player auraController, Permanent host) {
        Permanent aura = new Permanent(new ShiftingShadow());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
        return aura;
    }

    private Permanent addCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    @Test
    @DisplayName("Enchanted creature has haste")
    void enchantedCreatureHasHaste() {
        Permanent creature = addCreature(player1);
        attachShadow(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted controller's upkeep destroys the creature and reattaches to the revealed creature")
    void destroysAndRevealsForEnchantedController() {
        Permanent enchanted = addCreature(player2);
        Permanent aura = attachShadow(player1, enchanted);
        setDeck(player2, List.of(new Forest(), new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchanted);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        Permanent revealed = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(aura.getAttachedTo()).isEqualTo(revealed.getId());
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(1)
                .allMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Aura controller's upkeep does not trigger the ability")
    void doesNotTriggerDuringAuraControllerUpkeep() {
        Permanent enchanted = addCreature(player2);
        Permanent aura = attachShadow(player1, enchanted);
        setDeck(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted);
        assertThat(aura.getAttachedTo()).isEqualTo(enchanted.getId());
    }

    @Test
    @DisplayName("Without a creature in the library, the unattached Aura goes to the graveyard")
    void noCreatureFoundLeavesAuraToGraveyard() {
        Permanent enchanted = addCreature(player1);
        Permanent aura = attachShadow(player1, enchanted);
        setDeck(player1, List.of(new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(enchanted, aura);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"))
                .anyMatch(card -> card.getName().equals("Shifting Shadow"));
    }

    @Test
    @DisplayName("Can enchant only a creature")
    void cannotEnchantLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ShiftingShadow()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setDeck(Player player, List<? extends Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}

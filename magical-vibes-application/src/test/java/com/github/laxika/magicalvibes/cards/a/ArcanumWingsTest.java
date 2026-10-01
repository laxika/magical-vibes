package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FertileGround;
import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.cards.g.GiftOfGranite;
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

@CardUsed({ArcanumWings.class, FertileGround.class, FomoriNomad.class, GiftOfGranite.class})
class ArcanumWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Aura swap exchanges Arcanum Wings for an Aura from hand")
    void exchangesAuraForAuraFromHand() {
        Permanent creature = addCreatureReady(player1, new FomoriNomad());
        addAura(creature, new ArcanumWings());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        harness.setHand(player1, List.of(new GiftOfGranite()));
        addAuraSwapMana();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Arcanum Wings");
        harness.assertNotOnBattlefield(player1, "Arcanum Wings");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Gift of Granite")
                        && permanent.isAttached()
                        && creature.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Declining Aura swap leaves the source Aura attached")
    void decliningLeavesSourceAuraInPlay() {
        Permanent creature = addCreatureReady(player1, new FomoriNomad());
        addAura(creature, new ArcanumWings());
        harness.setHand(player1, List.of(new GiftOfGranite()));
        addAuraSwapMana();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInHand(player1, "Gift of Granite");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Arcanum Wings")
                        && permanent.isAttached()
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Aura swap does nothing when its source Aura is controlled but not owned")
    void controlledButNotOwnedAuraCannotSwap() {
        Permanent creature = addCreatureReady(player2, new FomoriNomad());
        ArcanumWings sourceCard = new ArcanumWings();
        sourceCard.setOwnerId(player1.getId());
        Permanent source = addAura(player2, creature, sourceCard);
        harness.setHand(player2, List.of(new GiftOfGranite()));
        addAuraSwapMana(player2);

        harness.activateAbility(player2, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(source);
        assertThat(source.isAttached()).isTrue();
        assertThat(source.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertInHand(player2, "Gift of Granite");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Aura swap cannot choose an Aura that cannot enchant the source host")
    void cannotChooseAuraWithIncompatibleEnchantRestriction() {
        Permanent creature = addCreatureReady(player1, new FomoriNomad());
        addAura(creature, new ArcanumWings());
        harness.setHand(player1, List.of(new FertileGround()));
        addAuraSwapMana();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fertile Ground");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Arcanum Wings")
                        && permanent.isAttached()
                        && creature.getId().equals(permanent.getAttachedTo()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addAura(Permanent creature, Card card) {
        return addAura(player1, creature, card);
    }

    private Permanent addAura(Player controller, Permanent creature, Card card) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, card);
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    private void addAuraSwapMana() {
        addAuraSwapMana(player1);
    }

    private void addAuraSwapMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}

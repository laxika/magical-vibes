package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HondenOfLifesWeb;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoShintaiOfLifesOrigin.class, HondenOfLifesWeb.class, GloriousAnthem.class, GrizzlyBears.class})
class GoShintaiOfLifesOriginTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry creates a 1/1 colorless Shrine enchantment creature token")
    void ownEntryCreatesShrineToken() {
        GoShintaiOfLifesOrigin card = new GoShintaiOfLifesOrigin();
        harness.setHand(player1, List.of(card));
        addFiveColorMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Shrine");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColors()).isEmpty();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Another nontoken Shrine entering creates a Shrine token")
    void anotherNontokenShrineEntryCreatesShrineToken() {
        harness.addToBattlefield(player1, new GoShintaiOfLifesOrigin());

        harness.enterBattlefieldAndReturn(player1, new HondenOfLifesWeb());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Shrine")).hasSize(1);
    }

    @Test
    @DisplayName("Its ability returns a target enchantment from the graveyard")
    void returnsTargetEnchantmentFromGraveyard() {
        Permanent source = addReadySource();
        Card enchantment = new GloriousAnthem();
        harness.setGraveyard(player1, List.of(enchantment));
        addFiveColorMana();

        harness.activateAbility(player1, 0, 0, null, enchantment.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(enchantment.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(enchantment.getId()));
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Its ability cannot target a nonenchantment card")
    void rejectsNonEnchantmentTarget() {
        addReadySource();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addFiveColorMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GoShintaiOfLifesOrigin());
        source.setSummoningSick(false);
        return source;
    }

    private void addFiveColorMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}

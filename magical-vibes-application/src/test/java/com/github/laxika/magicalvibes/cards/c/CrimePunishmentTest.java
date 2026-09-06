package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FavorableWinds;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrimePunishment.class, GrizzlyBears.class, FavorableWinds.class, Millstone.class,
        Juggernaut.class, SolRing.class})
class CrimePunishmentTest extends BaseCardTest {

    private static final int CRIME = 0;
    private static final int PUNISHMENT = 1;

    @Test
    @DisplayName("Crime puts a creature from an opponent's graveyard onto the battlefield under your control")
    void crimeReturnsCreatureFromOpponentGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new CrimePunishment()));
        addCrimeMana();

        harness.castSorcery(player1, 0, CRIME, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .contains(creature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Crime can put an enchantment from an opponent's graveyard onto the battlefield")
    void crimeReturnsEnchantmentFromOpponentGraveyard() {
        Card enchantment = new FavorableWinds();
        harness.setGraveyard(player2, List.of(enchantment));
        harness.setHand(player1, List.of(new CrimePunishment()));
        addCrimeMana();

        harness.castSorcery(player1, 0, CRIME, enchantment.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .contains(enchantment.getId());
    }

    @Test
    @DisplayName("Crime cannot target a card that is neither a creature nor an enchantment")
    void crimeCannotTargetArtifactCard() {
        Card artifact = new Millstone();
        harness.setGraveyard(player2, List.of(artifact));
        harness.setHand(player1, List.of(new CrimePunishment()));
        addCrimeMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, CRIME, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Punishment destroys artifacts, creatures, and enchantments with mana value X")
    void punishmentDestroysMatchingPermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new FavorableWinds());
        Permanent differentManaValue = harness.addToBattlefieldAndReturn(player2, new Juggernaut());
        Permanent differentType = harness.addToBattlefieldAndReturn(player2, new SolRing());

        harness.setHand(player1, List.of(new CrimePunishment()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{PUNISHMENT}, 2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .doesNotContain(creature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .doesNotContain(artifact.getId(), enchantment.getId())
                .contains(differentManaValue.getId(), differentType.getId());
    }

    private void addCrimeMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}

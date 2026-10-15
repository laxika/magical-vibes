package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BindTheMonster;
import com.github.laxika.magicalvibes.cards.f.FearlessPup;
import com.github.laxika.magicalvibes.cards.f.FrostBite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OpenTheOmenpaths.class, GrizzlyBears.class, FearlessPup.class,
        BindTheMonster.class, FrostBite.class})
class OpenTheOmenpathsTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts creatures you control until end of turn")
    void boostsOwnCreatures() {
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        harness.setHand(player1, List.of(new OpenTheOmenpaths()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        Permanent ownBears = findPermanent(player1, "Grizzly Bears");
        Permanent opposingBears = findPermanent(player2, "Grizzly Bears");
        assertThat(ownBears.getEffectivePower()).isEqualTo(3);
        assertThat(ownBears.getEffectiveToughness()).isEqualTo(2);
        assertThat(opposingBears.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds two mana of each of two different colors for creature or enchantment spells")
    void addsRestrictedManaForCreatureAndEnchantmentSpells() {
        harness.setHand(player1, List.of(new OpenTheOmenpaths()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureOrEnchantmentSpellOnlyMana(ManaColor.RED))
                .isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureOrEnchantmentSpellOnlyMana(ManaColor.BLUE))
                .isEqualTo(2);

        Card creature = simpleSpell("Restricted creature", CardType.CREATURE, "{R}");
        harness.setHand(player1, List.of(creature));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Restricted creature");

        Card enchantment = simpleSpell("Restricted enchantment", CardType.ENCHANTMENT, "{U}");
        harness.setHand(player1, List.of(enchantment));
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Restricted enchantment");

        Card instant = simpleSpell("Forbidden instant", CardType.INSTANT, "{U}");
        harness.setHand(player1, List.of(instant));
        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostExpiresAndDoesNotAffectCreaturesEnteringLater() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new FearlessPup());
        harness.setHand(player1, List.of(new OpenTheOmenpaths()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        Permanent later = harness.addToBattlefieldAndReturn(player1, new FearlessPup());
        assertThat(original.getEffectivePower()).isEqualTo(2);
        assertThat(original.getEffectiveToughness()).isEqualTo(1);
        assertThat(later.getEffectivePower()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureOrEnchantmentSpellOnlyManaTotal())
                .isZero();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(original.getEffectivePower()).isEqualTo(1);
        assertThat(original.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void restrictedManaCastsRealCreatureAndAuraButNotAnInstant() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FearlessPup());
        harness.setHand(player1, List.of(new OpenTheOmenpaths()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        assertThatThrownBy(() -> harness.handleListChoice(player1, "RED"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> harness.handleListChoice(player1, "COLORLESS"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, "BLUE");
        assertThat(target.getEffectivePower()).isEqualTo(1);

        harness.setHand(player1, List.of(new FearlessPup()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Fearless Pup");

        harness.setHand(player1, List.of(new BindTheMonster()));
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bind the Monster");
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureOrEnchantmentSpellOnlyMana(ManaColor.RED))
                .isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureOrEnchantmentSpellOnlyMana(ManaColor.BLUE))
                .isEqualTo(1);

        harness.setHand(player1, List.of(new FrostBite()));
        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Card simpleSpell(String name, CardType type, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost(manaCost);
        if (type == CardType.CREATURE) {
            card.setPower(2);
            card.setToughness(2);
        }
        return card;
    }
}

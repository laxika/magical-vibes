package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FistsOfTheDemigodTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor... colors) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColors(List.of(colors));
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private Permanent attachFists(Permanent creature) {
        gd.playerBattlefields.get(player1.getId()).add(creature);
        Permanent fists = new Permanent(new FistsOfTheDemigod());
        fists.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(fists);
        return fists;
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Resolving Fists of the Demigod attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent target = new Permanent(createCreature("Necro Zombie", 2, 2, CardColor.BLACK));
        target.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new FistsOfTheDemigod()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Fists of the Demigod")
                        && p.isAttached()
                        && p.getAttachedTo().equals(target.getId()));
    }

    // ===== Black enchanted creature: +1/+1 and wither =====

    @Test
    @DisplayName("Black enchanted creature gets +1/+1 and wither, but not first strike")
    void blackCreatureGetsBoostAndWither() {
        Permanent black = new Permanent(createCreature("Necro Zombie", 2, 2, CardColor.BLACK));
        attachFists(black);

        assertThat(gqs.getEffectivePower(gd, black)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, black)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, black, Keyword.WITHER)).isTrue();
        assertThat(gqs.hasKeyword(gd, black, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Red enchanted creature: +1/+1 and first strike =====

    @Test
    @DisplayName("Red enchanted creature gets +1/+1 and first strike, but not wither")
    void redCreatureGetsBoostAndFirstStrike() {
        Permanent red = new Permanent(new HillGiant()); // 3/3 red
        attachFists(red);

        assertThat(gqs.getEffectivePower(gd, red)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, red)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, red, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, red, Keyword.WITHER)).isFalse();
    }

    // ===== Black-red enchanted creature: both bonuses stack =====

    @Test
    @DisplayName("Black and red enchanted creature gets +2/+2, wither, and first strike")
    void blackRedCreatureGetsBothBonuses() {
        Permanent blackRed = new Permanent(createCreature("Rakdos Brute", 2, 2, CardColor.BLACK, CardColor.RED));
        attachFists(blackRed);

        assertThat(gqs.getEffectivePower(gd, blackRed)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, blackRed)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, blackRed, Keyword.WITHER)).isTrue();
        assertThat(gqs.hasKeyword(gd, blackRed, Keyword.FIRST_STRIKE)).isTrue();
    }

    // ===== Neither black nor red: no bonuses =====

    @Test
    @DisplayName("A creature that is neither black nor red gets no bonuses")
    void nonBlackNonRedGetsNothing() {
        Permanent green = new Permanent(new GrizzlyBears()); // 2/2 green
        attachFists(green);

        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, green)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, green, Keyword.WITHER)).isFalse();
        assertThat(gqs.hasKeyword(gd, green, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Bonuses fall off when the aura leaves =====

    @Test
    @DisplayName("Bonuses are removed when Fists of the Demigod leaves the battlefield")
    void bonusesRemovedWhenAuraRemoved() {
        Permanent black = new Permanent(createCreature("Necro Zombie", 2, 2, CardColor.BLACK));
        Permanent fists = attachFists(black);

        assertThat(gqs.getEffectivePower(gd, black)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, black, Keyword.WITHER)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(fists);

        assertThat(gqs.getEffectivePower(gd, black)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, black)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, black, Keyword.WITHER)).isFalse();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Fists of the Demigod")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new FistsOfTheDemigod()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}

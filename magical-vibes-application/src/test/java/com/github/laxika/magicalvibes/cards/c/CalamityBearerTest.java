package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalamityBearerTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles combat damage from a Giant source")
    void doublesGiantCombatDamage() {
        harness.addToBattlefield(player1, new CalamityBearer());
        Permanent giant = addCreatureReady(player1, createCreature("Giant", 2, 2, CardSubtype.GIANT));
        giant.setAttacking(true);

        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not double combat damage from a non-Giant source")
    void doesNotDoubleNonGiantCombatDamage() {
        harness.addToBattlefield(player1, new CalamityBearer());
        Permanent elf = addCreatureReady(player1, createCreature("Elf", 2, 2, CardSubtype.ELF));
        elf.setAttacking(true);

        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Doubles noncombat damage from a Giant source")
    void doublesGiantNoncombatDamage() {
        harness.addToBattlefield(player1, new CalamityBearer());
        addCreatureReady(player1, createDamageCreature("Giant", CardSubtype.GIANT));

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not double damage from a Giant controlled by an opponent")
    void doesNotDoubleOpponentsGiantDamage() {
        harness.addToBattlefield(player1, new CalamityBearer());
        addCreatureReady(player2, createDamageCreature("Giant", CardSubtype.GIANT));
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    private Card createCreature(String name, int power, int toughness, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.RED);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtype));
        return card;
    }

    private Card createDamageCreature(String name, CardSubtype subtype) {
        Card card = createCreature(name, 1, 1, subtype);
        card.addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Deal 1 damage to any target."
        ));
        return card;
    }
}

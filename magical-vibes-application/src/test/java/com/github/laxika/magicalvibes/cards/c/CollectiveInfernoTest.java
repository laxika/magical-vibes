package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CollectiveInfernoTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type when Collective Inferno enters stores the choice")
    void choosesCreatureTypeOnEntry() {
        harness.setHand(player1, List.of(new CollectiveInferno()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(findPermanent(player1, "Collective Inferno").getChosenSubtype())
                .isEqualTo(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Doubles combat damage from sources of the chosen type")
    void doublesMatchingCombatDamage() {
        addCollectiveInferno();
        Permanent goblin = addCreatureReady(player1, createCreature("Goblin", 2, 2, CardSubtype.GOBLIN));
        goblin.setAttacking(true);

        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not double combat damage from sources of another type")
    void doesNotDoubleOtherCombatDamage() {
        addCollectiveInferno();
        Permanent elf = addCreatureReady(player1, createCreature("Elf", 2, 2, CardSubtype.ELF));
        elf.setAttacking(true);

        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Doubles noncombat damage from a matching creature source")
    void doublesMatchingNoncombatDamage() {
        addCollectiveInferno();
        Permanent goblin = addCreatureReady(player1, createDamageCreature("Goblin", CardSubtype.GOBLIN));

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(goblin.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    private Permanent addCollectiveInferno() {
        Permanent perm = new Permanent(new CollectiveInferno());
        perm.setChosenSubtype(CardSubtype.GOBLIN);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
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

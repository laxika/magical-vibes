package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CollectiveInferno.class, RagingGoblin.class, Tarfire.class})
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

    @Test
    void convokePaysColoredManaWithFreshCreatures() {
        harness.setHand(player1, List.of(new CollectiveInferno()));
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new RagingGoblin());
        List<Permanent> goblins = findPermanents(player1, "Raging Goblin");
        goblins.forEach(goblin -> goblin.setSummoningSick(true));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                goblins.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        harness.assertOnBattlefield(player1, "Collective Inferno");
        assertThat(goblins).allMatch(Permanent::isTapped);
        assertThat(findPermanent(player1, "Collective Inferno").getChosenSubtype())
                .isEqualTo(CardSubtype.GOBLIN);
    }

    @Test
    void doublesMatchingKindredSpellDamage() {
        addCollectiveInferno();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 16);
    }

    @Test
    void doesNotDoubleOpponentsMatchingSpellDamage() {
        addCollectiveInferno();
        harness.setHand(player2, List.of(new Tarfire()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    void doublesDamageToItsController() {
        addCollectiveInferno();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 16);
    }

    @Test
    void multipleCopiesMultiplyMatchingDamage() {
        addCollectiveInferno();
        addCollectiveInferno();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 12);
    }

    @Test
    void doesNotDoubleOpponentsMatchingCombatDamage() {
        addCollectiveInferno();
        Permanent goblin = addCreatureReady(player2, new RagingGoblin());
        goblin.setAttacking(true);

        resolveCombat(player2);

        harness.assertLife(player1, 19);
    }
    @Test
    void chosenTypeLimitsKindredSpellDamage() {
        addCollectiveInferno().setChosenSubtype(CardSubtype.ELF);
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }
    private Permanent addCollectiveInferno() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new CollectiveInferno());
        perm.setChosenSubtype(CardSubtype.GOBLIN);
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

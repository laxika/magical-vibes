package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BasaltRavager;
import com.github.laxika.magicalvibes.cards.f.FrostBite;
import com.github.laxika.magicalvibes.cards.m.MaskedVandal;
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

@CardUsed({CalamityBearer.class, BasaltRavager.class, MaskedVandal.class, FrostBite.class})
class CalamityBearerTest extends BaseCardTest {

    @Test
    void doublesItsOwnCombatDamage() {
        Permanent bearer = addCreatureReady(player1, new CalamityBearer());
        bearer.setAttacking(true);

        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    void twoBearersQuadrupleDamage() {
        harness.addToBattlefield(player1, new CalamityBearer());
        Permanent bearer = addCreatureReady(player1, new CalamityBearer());
        bearer.setAttacking(true);

        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(8);
    }

    @Test
    void doublesChangelingCombatDamage() {
        harness.addToBattlefield(player1, new CalamityBearer());
        Permanent changeling = addCreatureReady(player1, new MaskedVandal());
        changeling.setAttacking(true);

        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void doublesGiantTriggeredDamageToPlayer() {
        harness.addToBattlefield(player1, new CalamityBearer());
        castRavager(player2.getId());

        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void doublesGiantDamageToOpponentsPermanent() {
        harness.addToBattlefield(player1, new CalamityBearer());
        harness.addToBattlefield(player2, new CalamityBearer());
        castRavager(harness.getPermanentId(player2, "Calamity Bearer"));

        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Calamity Bearer");
        harness.assertOnBattlefield(player1, "Calamity Bearer");
    }

    @Test
    void doublesGiantDamageToOwnPermanent() {
        harness.addToBattlefield(player1, new CalamityBearer());
        castRavager(harness.getPermanentId(player1, "Calamity Bearer"));

        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Calamity Bearer");
    }

    private void castRavager(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new BasaltRavager()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0, targetId);
    }

    @Test
    void doublesTriggeredDamageAfterGiantSourceDies() {
        harness.addToBattlefield(player1, new CalamityBearer());
        castRavager(player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new FrostBite()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Basalt Ravager"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Basalt Ravager");
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void stopsDoublingWhenBearerLeavesBeforeDamage() {
        harness.addToBattlefield(player1, new CalamityBearer());
        castRavager(player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new FrostBite(), new FrostBite()));
        harness.addMana(player1, ManaColor.RED, 2);
        java.util.UUID bearerId = harness.getPermanentId(player1, "Calamity Bearer");
        harness.castInstant(player1, 0, bearerId);
        harness.castInstant(player1, 0, bearerId);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Calamity Bearer");
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

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

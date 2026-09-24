package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaldraCompleat.class, GrizzlyBears.class})
class KaldraCompleatTest extends BaseCardTest {

    @Test
    @DisplayName("Living weapon creates and equips a Phyrexian Germ")
    void livingWeaponCreatesAndEquipsGerm() {
        castKaldraCompleat();

        Permanent kaldra = findPermanent(player1, "Kaldra Compleat");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        assertThat(kaldra.getAttachedTo()).isEqualTo(germ.getId());
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, germ, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, germ, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, germ, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, germ, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, kaldra, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature exiles a creature it deals combat damage to")
    void equippedCreatureExilesCreatureItDamages() {
        castKaldraCompleat();
        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        Permanent blocker = addCreatureReady(player2, largeTestCreature());

        int germIndex = gd.playerBattlefields.get(player1.getId()).indexOf(germ);
        germ.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(germIndex);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.handleCombatDamageAssigned(player1, germIndex, Map.of(blocker.getId(), 5));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .contains(blocker.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(blocker.getCard());
    }

    @Test
    @DisplayName("Equip can move Kaldra Compleat to another creature")
    void equipMovesToAnotherCreature() {
        castKaldraCompleat();
        Permanent newHost = addCreatureReady(player1, new GrizzlyBears());
        Permanent kaldra = findPermanent(player1, "Kaldra Compleat");

        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(kaldra), null,
                newHost.getId());
        harness.passBothPriorities();

        assertThat(kaldra.getAttachedTo()).isEqualTo(newHost.getId());
    }

    private void castKaldraCompleat() {
        harness.setHand(player1, List.of(new KaldraCompleat()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Card largeTestCreature() {
        Card card = new Card();
        card.setName("Large Test Creature");
        card.setType(CardType.CREATURE);
        card.setPower(0);
        card.setToughness(7);
        return card;
    }
}

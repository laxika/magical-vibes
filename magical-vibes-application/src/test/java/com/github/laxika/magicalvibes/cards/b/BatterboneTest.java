package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.j.JadeAvenger;
import com.github.laxika.magicalvibes.cards.c.ChatterfangSquirrelGeneral;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Batterbone.class, JadeAvenger.class, ChatterfangSquirrelGeneral.class})
class BatterboneTest extends BaseCardTest {

    @Test
    void livingWeaponCreatesAndAttachesGerm() {
        harness.setHand(player1, List.of(new Batterbone()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent batterbone = findPermanent(player1, "Batterbone");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        assertThat(batterbone.getAttachedTo()).isEqualTo(germ.getId());
    }

    @Test
    void equippedCreatureGetsBoostAndKeywords() {
        Permanent creature = addCreatureReady(player1, new JadeAvenger());
        Permanent batterbone = harness.addToBattlefieldAndReturn(player1, new Batterbone());
        batterbone.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void equipAbilityMovesBatterboneToAnotherCreature() {
        Permanent firstCreature = addCreatureReady(player1, new JadeAvenger());
        Permanent secondCreature = addCreatureReady(player1, new JadeAvenger());
        Permanent batterbone = harness.addToBattlefieldAndReturn(player1, new Batterbone());
        batterbone.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 2, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(batterbone.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(3);
    }

    @Test
    void germAttacksWithoutTappingAndGainsLifeFromCombatDamage() {
        harness.setHand(player1, List.of(new Batterbone()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        germ.setSummoningSick(false);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(germ)));
        resolveCombat();

        assertThat(germ.isTapped()).isFalse();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void movingEquipmentAwayFromGermCausesItToDie() {
        Permanent creature = addCreatureReady(player1, new JadeAvenger());
        harness.setHand(player1, List.of(new Batterbone()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        Permanent batterbone = findPermanent(player1, "Batterbone");
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(batterbone), null, creature.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
        assertThat(batterbone.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void controllerCanChooseGermInsteadOfAdditionalSquirrelForLivingWeapon() {
        harness.addToBattlefield(player1, new ChatterfangSquirrelGeneral());
        harness.setHand(player1, List.of(new Batterbone()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        harness.handlePermanentChosen(player1, germ.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Batterbone").getAttachedTo()).isEqualTo(germ.getId());
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
        assertThat(countPermanents(player1, "Squirrel")).isEqualTo(1);
    }
}

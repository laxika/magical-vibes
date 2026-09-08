package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlacksmithsTalent.class, GrizzlyBears.class, LeoninScimitar.class})
class BlacksmithsTalentTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Sword Equipment token that can equip a creature")
    void createsSwordToken() {
        harness.setHand(player1, List.of(new BlacksmithsTalent()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent sword = findPermanent(player1, "Sword");
        Permanent creature = addReady(player1, new GrizzlyBears());
        assertThat(sword.getCard().getSubtypes()).contains(CardSubtype.EQUIPMENT);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int swordIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sword);
        harness.activateAbility(player1, swordIndex, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("At level 2, attaches an Equipment to up to one creature you control at combat")
    void levelTwoAttachesEquipmentAtBeginningOfCombat() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new BlacksmithsTalent());
        Permanent equipment = addReady(player1, new LeoninScimitar());
        Permanent creature = addReady(player1, new GrizzlyBears());
        levelUp(player1, talent, 0);

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("At level 3, equipped creatures have double strike and haste only during your turn")
    void levelThreeGrantsKeywordsToEquippedCreaturesDuringYourTurn() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new BlacksmithsTalent());
        Permanent equippedCreature = addReady(player1, new GrizzlyBears());
        Permanent unequippedCreature = addReady(player1, new GrizzlyBears());
        Permanent equipment = addReady(player1, new LeoninScimitar());
        equipment.setAttachedTo(equippedCreature.getId());

        levelUp(player1, talent, 0);
        levelUp(player1, talent, 1);

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, equippedCreature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, equippedCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, unequippedCreature, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, unequippedCreature, Keyword.HASTE)).isFalse();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, equippedCreature, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, equippedCreature, Keyword.HASTE)).isFalse();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void levelUp(Player player, Permanent talent, int abilityIndex) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, abilityIndex == 0 ? 2 : 3);
        int talentIndex = gd.playerBattlefields.get(player.getId()).indexOf(talent);
        harness.activateAbility(player, talentIndex, abilityIndex, null, null);
        harness.passBothPriorities();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}

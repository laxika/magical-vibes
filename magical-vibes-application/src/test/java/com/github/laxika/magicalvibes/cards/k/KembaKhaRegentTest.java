package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenPerEquipmentOnSourceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KembaKhaRegentTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private Permanent attachEquipment(Player player, LeoninScimitar equipment, UUID attachToId) {
        Permanent equipPerm = new Permanent(equipment);
        equipPerm.setAttachedTo(attachToId);
        gd.playerBattlefields.get(player.getId()).add(equipPerm);
        return equipPerm;
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Kemba has upkeep-triggered CreateTokenPerEquipmentOnSourceEffect")
    void hasCorrectProperties() {
        KembaKhaRegent card = new KembaKhaRegent();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(CreateTokenPerEquipmentOnSourceEffect.class);
    }

    // ===== No equipment attached =====

    @Test
    @DisplayName("No tokens created when no equipment is attached")
    void noTokensWhenNoEquipment() {
        harness.addToBattlefield(player1, new KembaKhaRegent());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).isEmpty();
    }

    // ===== One equipment attached =====

    @Test
    @DisplayName("Creates one 2/2 white Cat token when one equipment is attached")
    void createsOneTokenWithOneEquipment() {
        harness.addToBattlefield(player1, new KembaKhaRegent());
        UUID kembaId = harness.getPermanentId(player1, "Kemba, Kha Regent");

        attachEquipment(player1, new LeoninScimitar(), kembaId);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(1);
        Permanent catToken = tokens.getFirst();
        assertThat(catToken.getCard().getName()).isEqualTo("Cat");
        assertThat(catToken.getCard().getPower()).isEqualTo(2);
        assertThat(catToken.getCard().getToughness()).isEqualTo(2);
        assertThat(catToken.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(catToken.getCard().getSubtypes()).containsExactly(CardSubtype.CAT);
        assertThat(catToken.getCard().getType()).isEqualTo(CardType.CREATURE);
    }

    // ===== Two equipment attached =====

    @Test
    @DisplayName("Creates two Cat tokens when two equipment are attached")
    void createsTwoTokensWithTwoEquipment() {
        harness.addToBattlefield(player1, new KembaKhaRegent());
        UUID kembaId = harness.getPermanentId(player1, "Kemba, Kha Regent");

        attachEquipment(player1, new LeoninScimitar(), kembaId);
        attachEquipment(player1, new LeoninScimitar(), kembaId);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(2);
    }

    // ===== Equipment on other creatures doesn't count =====

    @Test
    @DisplayName("Equipment attached to other creatures does not count for Kemba")
    void equipmentOnOtherCreatureDoesNotCount() {
        harness.addToBattlefield(player1, new KembaKhaRegent());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        // Attach equipment to Grizzly Bears, not to Kemba
        attachEquipment(player1, new LeoninScimitar(), bearsId);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve Kemba's trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).isEmpty();
    }

    // ===== Doesn't trigger during opponent's upkeep =====

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new KembaKhaRegent());
        UUID kembaId = harness.getPermanentId(player1, "Kemba, Kha Regent");

        attachEquipment(player1, new LeoninScimitar(), kembaId);

        advanceToUpkeep(player2); // opponent's upkeep

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).isEmpty();
    }

    // ===== Tokens accumulate over multiple upkeeps =====

    @Test
    @DisplayName("Creates tokens on each upkeep, accumulating over multiple turns")
    void tokensAccumulateOverMultipleUpkeeps() {
        harness.addToBattlefield(player1, new KembaKhaRegent());
        UUID kembaId = harness.getPermanentId(player1, "Kemba, Kha Regent");

        attachEquipment(player1, new LeoninScimitar(), kembaId);

        // First upkeep
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Second upkeep
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(2);
    }
}

package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.Arrest;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KembaKhaRegent.class, LeoninScimitar.class, GrizzlyBears.class, Disperse.class, Arrest.class})
class KembaKhaRegentTest extends BaseCardTest {

    private Permanent attachEquipment(Player player, LeoninScimitar equipment, UUID attachToId) {
        Permanent equipPerm = harness.addToBattlefieldAndReturn(player, equipment);
        equipPerm.setAttachedTo(attachToId);
        return equipPerm;
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

    @Test
    void equipmentControlledByOpponentStillCounts() {
        Permanent kemba = harness.addToBattlefieldAndReturn(player1, new KembaKhaRegent());
        attachEquipment(player2, new LeoninScimitar(), kemba.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList()).isEmpty();
    }

    @Test
    void auraAttachedToKembaDoesNotCount() {
        Permanent kemba = harness.addToBattlefieldAndReturn(player1, new KembaKhaRegent());
        Permanent arrest = harness.addToBattlefieldAndReturn(player2, new Arrest());
        arrest.setAttachedTo(kemba.getId());
        attachEquipment(player1, new LeoninScimitar(), kemba.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList()).hasSize(1);
    }

    @Test
    void equipmentRemovedInResponseIsNotCounted() {
        Permanent kemba = harness.addToBattlefieldAndReturn(player1, new KembaKhaRegent());
        Permanent equipment = attachEquipment(player1, new LeoninScimitar(), kemba.getId());
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        advanceToUpkeep(player1);
        harness.castAndResolveInstant(player1, 0, equipment.getId());
        harness.assertInHand(player1, "Leonin Scimitar");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList()).isEmpty();
    }

    @Test
    void usesLastKnownEquipmentCountWhenKembaLeavesBeforeResolution() {
        Permanent kemba = harness.addToBattlefieldAndReturn(player1, new KembaKhaRegent());
        attachEquipment(player1, new LeoninScimitar(), kemba.getId());
        attachEquipment(player1, new LeoninScimitar(), kemba.getId());
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        advanceToUpkeep(player1);
        harness.castAndResolveInstant(player1, 0, kemba.getId());
        harness.assertNotOnBattlefield(player1, "Kemba, Kha Regent");
        harness.assertInHand(player1, "Kemba, Kha Regent");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList()).hasSize(2);
    }
}

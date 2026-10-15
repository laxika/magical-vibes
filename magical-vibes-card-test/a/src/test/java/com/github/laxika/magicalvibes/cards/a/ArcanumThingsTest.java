package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KondasBanner;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcanumThings.class, GrizzlyBears.class, KondasBanner.class, LeoninScimitar.class})
class ArcanumThingsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipment swap exchanges Arcanum Things for an Equipment from hand")
    void exchangesEquipmentForEquipmentFromHand() {
        Permanent creature = addCreature(new GrizzlyBears());
        addEquipment(creature, new ArcanumThings());
        harness.setHand(player1, List.of(new LeoninScimitar()));
        addSwapMana();

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Arcanum Things");
        harness.assertNotOnBattlefield(player1, "Arcanum Things");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Leonin Scimitar")
                        && permanent.isAttached()
                        && creature.getId().equals(permanent.getAttachedTo()));
        assertThat(harness.getGameQueryService().hasKeyword(gd, creature,
                Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Declining Equipment swap leaves the source Equipment attached")
    void decliningLeavesSourceEquipmentInPlay() {
        Permanent creature = addCreature(new GrizzlyBears());
        addEquipment(creature, new ArcanumThings());
        harness.setHand(player1, List.of(new LeoninScimitar()));
        addSwapMana();

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInHand(player1, "Leonin Scimitar");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Arcanum Things")
                        && permanent.isAttached()
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Equipment swap does not offer an Equipment that cannot attach")
    void incompatibleEquipmentCannotBeChosen() {
        Permanent creature = addCreature(new GrizzlyBears());
        addEquipment(creature, new ArcanumThings());
        harness.setHand(player1, List.of(new KondasBanner()));
        addSwapMana();

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Konda's Banner");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Arcanum Things")
                        && permanent.isAttached()
                        && creature.getId().equals(permanent.getAttachedTo()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Equip grants flying only to the equipped creature")
    void equipGrantsFlying() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new ArcanumThings());
        Permanent creature = addCreature(new GrizzlyBears());
        Permanent other = addCreature(new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Unattached Equipment can be exchanged without attaching the replacement")
    void swapsUnattachedEquipment() {
        harness.addToBattlefield(player1, new ArcanumThings());
        harness.setHand(player1, List.of(new LeoninScimitar()));
        addSwapMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Arcanum Things");
        harness.assertNotOnBattlefield(player1, "Arcanum Things");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Leonin Scimitar")
                        && !permanent.isAttached());
    }

    @Test
    @DisplayName("Equipment swap cannot exchange an Equipment owned by another player")
    void cannotSwapEquipmentOwnedByOpponent() {
        Permanent creature = addCreature(new GrizzlyBears());
        ArcanumThings card = new ArcanumThings();
        card.setOwnerId(player2.getId());
        addEquipment(creature, card);
        harness.setHand(player1, List.of(new LeoninScimitar()));
        addSwapMana();

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Leonin Scimitar");
        harness.assertOnBattlefield(player1, "Arcanum Things");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Equipment swap does nothing when there is no Equipment in hand")
    void noEquipmentInHand() {
        Permanent creature = addCreature(new GrizzlyBears());
        Permanent equipment = addEquipment(creature, new ArcanumThings());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addSwapMana();

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Arcanum Things");
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Equipment swap does nothing if its source has left the battlefield")
    void sourceMustRemainOnBattlefield() {
        Permanent creature = addCreature(new GrizzlyBears());
        Permanent equipment = addEquipment(creature, new ArcanumThings());
        harness.setHand(player1, List.of(new LeoninScimitar()));
        addSwapMana();

        harness.activateAbility(player1, 1, 1, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(equipment);
        gd.playerGraveyards.get(player1.getId()).add(equipment.getCard());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Leonin Scimitar");
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addCreature(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addEquipment(Permanent creature, Card card) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, card);
        equipment.setAttachedTo(creature.getId());
        return equipment;
    }

    private void addSwapMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}

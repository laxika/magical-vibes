package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhiplashVengefulEngineer.class, LeoninScimitar.class, GrizzlyBears.class})
class WhiplashVengefulEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped attack drains life equal to the number of attached Equipment")
    void equippedAttackDrainsForEachEquipment() {
        Permanent whiplash = addCreatureReady(player1, new WhiplashVengefulEngineer());
        attachEquipment(whiplash);
        attachEquipment(whiplash);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The attack ability does not trigger when Whiplash is not equipped")
    void attackDoesNotTriggerWhenNotEquipped() {
        addCreatureReady(player1, new WhiplashVengefulEngineer());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The ability does nothing if Whiplash becomes unequipped before resolution")
    void abilityDoesNothingIfUnequippedBeforeResolution() {
        Permanent whiplash = addCreatureReady(player1, new WhiplashVengefulEngineer());
        Permanent equipment = attachEquipment(whiplash);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        equipment.setAttachedTo(null);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private Permanent attachEquipment(Permanent host) {
        Permanent equipment = new Permanent(new LeoninScimitar());
        equipment.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(equipment);
        return equipment;
    }
}

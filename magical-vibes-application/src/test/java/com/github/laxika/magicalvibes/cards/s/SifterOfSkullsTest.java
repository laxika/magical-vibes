package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({SifterOfSkulls.class, GrizzlyBears.class, Shock.class})
class SifterOfSkullsTest extends BaseCardTest {

    @Test
    @DisplayName("Another nontoken creature you control dying creates an Eldrazi Scion")
    void ownNontokenCreatureDeathCreatesScion() {
        harness.addToBattlefield(player1, new SifterOfSkulls());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroyWithShock(player2, creature.getId());

        Permanent scion = findPermanent(player1, "Eldrazi Scion");
        assertThat(scion.getCard().isToken()).isTrue();
        assertThat(scion.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(scion.getCard().getColor()).isNull();
        assertThat(scion.getCard().getSubtypes()).containsExactly(CardSubtype.ELDRAZI, CardSubtype.SCION);
        assertThat(scion.getEffectivePower()).isEqualTo(1);
        assertThat(scion.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Token and opposing creature deaths do not trigger Sifter of Skulls")
    void tokenAndOpposingCreatureDeathsDoNotTrigger() {
        harness.addToBattlefield(player1, new SifterOfSkulls());

        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        destroyWithShock(player2, token.getId());
        destroyWithShock(player1, opposingCreature.getId());

        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
    }

    @Test
    @DisplayName("An Eldrazi Scion can be sacrificed for colorless mana")
    void scionCanBeSacrificedForColorlessMana() {
        harness.addToBattlefield(player1, new SifterOfSkulls());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        destroyWithShock(player2, creature.getId());

        Permanent scion = findPermanent(player1, "Eldrazi Scion");
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, null, null);

        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private void destroyWithShock(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

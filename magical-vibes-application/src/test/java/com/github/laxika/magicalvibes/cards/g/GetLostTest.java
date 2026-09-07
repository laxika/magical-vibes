package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GetLost.class, GrizzlyBears.class, Forest.class, GloriousAnthem.class})
class GetLostTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature and creates two Map tokens")
    void destroysCreatureAndCreatesMaps() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Map")).hasSize(2);
        assertThat(findPermanents(player1, "Map")).allMatch(map ->
                map.getCard().hasType(CardType.ARTIFACT)
                        && map.getCard().getSubtypes().contains(CardSubtype.MAP));
    }

    @Test
    @DisplayName("Can destroy an enchantment")
    void destroysEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast(target);

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        assertThat(findPermanents(player1, "Map")).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a land")
    void rejectsLandTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new GetLost()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Map token sacrifices itself to make a creature explore at sorcery speed")
    void mapExploresCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent toDestroy = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        cast(toDestroy);
        Permanent map = findPermanents(player1, "Map").getFirst();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, battlefieldIndex(player1, map), 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(land.getId());
        assertThat(findPermanents(player1, "Map")).hasSize(1);
    }

    @Test
    @DisplayName("A Map token cannot target an opponent's creature")
    void mapRequiresCreatureYouControl() {
        Permanent map = addMap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, map), 0,
                null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GetLost()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addMap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target);
        return findPermanents(player1, "Map").getFirst();
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}

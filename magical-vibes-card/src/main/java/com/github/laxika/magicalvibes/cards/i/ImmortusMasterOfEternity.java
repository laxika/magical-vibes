package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.amount.CardsDrawnThisTurn;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "623")
public class ImmortusMasterOfEternity extends Card {

    public ImmortusMasterOfEternity() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.BLUE,
                        new CardsDrawnThisTurn(),
                        new ManaRestriction.NoncreatureSpells())),
                "{T}: Add {U} for each card you've drawn this turn. Spend this mana only to cast noncreature spells."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{U}{U}",
                List.of(
                        new EachPlayerShufflesZonesIntoLibraryEffect(),
                        new EachPlayerDrawsCardEffect(7),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "Power-up — {5}{U}{U}: Each player shuffles their hand and graveyard into their library, then draws seven cards. "
                        + "Put a +1/+1 counter on Immortus. Activate each power-up ability only once. Reduce the cost by his mana cost if he entered this turn."
        ).withPowerUp());
    }
}

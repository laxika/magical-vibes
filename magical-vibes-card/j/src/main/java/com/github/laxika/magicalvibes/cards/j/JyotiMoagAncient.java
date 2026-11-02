package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CommanderCastsFromCommandZoneThisGame;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "8")
@CardRegistration(set = "M3C", collectorNumber = "13")
@CardRegistration(set = "M3C", collectorNumber = "21")
@CardRegistration(set = "M3C", collectorNumber = "29")
@CardRegistration(set = "M3C", collectorNumber = "140")
public class JyotiMoagAncient extends Card {

    public JyotiMoagAncient() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                new CommanderCastsFromCommandZoneThisGame(),
                "Forest Dryad",
                1,
                1,
                CardColor.GREEN,
                List.of(CardSubtype.FOREST, CardSubtype.DRYAD),
                Set.of(),
                Set.of(CardType.LAND)
        ));

        addTrigger(Triggers.beginningOfCombat(ActivePlayer.ANY),
                new BoostAllOwnCreaturesEffect(
                        new SourcePower(),
                        new SourcePower(),
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsLandPredicate()))));
    }
}

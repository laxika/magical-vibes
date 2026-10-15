package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AnimateTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "368")
@CardRegistration(set = "MB2", collectorNumber = "607")
public class Microscope extends Card {

    public Microscope() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SurveilEffect(1)),
                "{T}: Surveil 1."));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new AnimateTargetGraveyardCardEffect(
                        0,
                        0,
                        Set.of(CardColor.BLACK),
                        List.of(CardSubtype.GERM),
                        Set.of(CardType.CREATURE))),
                "{T}: Target permanent card in a graveyard becomes a 0/0 black Germ creature in addition "
                        + "to its other colors and types until end of turn. (This effect ends if it leaves the graveyard.)"));
    }
}

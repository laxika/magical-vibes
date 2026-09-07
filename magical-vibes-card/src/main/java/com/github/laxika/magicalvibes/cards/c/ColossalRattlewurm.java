package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "159")
public class ColossalRattlewurm extends Card {

    public ColossalRattlewurm() {
        setFlashCastCondition(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.DESERT)));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.DESERT),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED)),
                "{1}{G}, Exile this card from your graveyard: Search your library for a Desert card, "
                        + "put it onto the battlefield tapped, then shuffle."));
    }
}

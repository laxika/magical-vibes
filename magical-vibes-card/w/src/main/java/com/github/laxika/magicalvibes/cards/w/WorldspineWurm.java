package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RTR", collectorNumber = "140")
@CardRegistration(set = "SLD", collectorNumber = "1305")
@CardRegistration(set = "EA3", collectorNumber = "7")
@CardRegistration(set = "DSC", collectorNumber = "206")
public class WorldspineWurm extends Card {

    public WorldspineWurm() {
        // When this creature dies, create three 5/5 green Wurm creature tokens with trample.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenEffect(
                3, "Wurm", 5, 5, CardColor.GREEN,
                List.of(CardSubtype.WURM),
                Set.of(Keyword.TRAMPLE),
                Set.of()));

        // When Worldspine Wurm is put into a graveyard from anywhere, shuffle it into its owner's library.
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.THIS), new ShuffleSelfFromGraveyardIntoLibraryEffect());
    }
}

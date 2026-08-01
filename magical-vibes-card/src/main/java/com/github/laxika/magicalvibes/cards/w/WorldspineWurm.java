package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RTR", collectorNumber = "140")
public class WorldspineWurm extends Card {

    public WorldspineWurm() {
        // When this creature dies, create three 5/5 green Wurm creature tokens with trample.
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                3, "Wurm", 5, 5, CardColor.GREEN,
                List.of(CardSubtype.WURM),
                Set.of(Keyword.TRAMPLE),
                Set.of()));

        // When Worldspine Wurm is put into a graveyard from anywhere, shuffle it into its owner's library.
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, new ShuffleSelfFromGraveyardIntoLibraryEffect());
    }
}
